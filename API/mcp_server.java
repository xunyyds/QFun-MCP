int getPort() {
    try {
        int p = getInt("mcp", "port", 8765);
        if (p > 0 && p < 65536) return p;
    } catch (Exception e) {}
    return 8765;
}

String getToken() {
    try {
        String t = getString("mcp", "token", "");
        if (t == null || t.length() == 0) {
            t = UUID.randomUUID().toString().replace("-", "");
            putString("mcp", "token", t);
        }
        return t;
    } catch (Exception e) {
        return "";
    }
}

ArrayList getLocalIps() {
    ArrayList r = new ArrayList();
    try {
        Enumeration ens = NetworkInterface.getNetworkInterfaces();
        while (ens != null && ens.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) ens.nextElement();
            try {
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = (InetAddress) addrs.nextElement();
                    if (!a.isLoopbackAddress() && a instanceof Inet4Address && a.isSiteLocalAddress()) {
                        String s = a.getHostAddress();
                        if (!r.contains(s)) r.add(s);
                    }
                }
            } catch (Exception e) {}
        }
    } catch (Exception e) {}
    if (r.size() == 0) r.add("127.0.0.1");
    return r;
}

String getLocalIp() {
    try {
        ArrayList ips = getLocalIps();
        if (ips.size() > 0) return (String) ips.get(0);
    } catch (Exception e) {}
    return "127.0.0.1";
}

void startServer() {
    int port = getPort();
    ServerSocket ss = null;
    int[] tries = new int[]{port, 8765, 9876, 18765};
    for (int i = 0; i < tries.length && ss == null; i++) {
        try {
            ServerSocket x = new ServerSocket();
            x.bind(new InetSocketAddress(tries[i]));
            ss = x;
        } catch (Exception e) {
            log("/Log/mcp.txt", "绑定端口失败 " + tries[i] + ": " + e.getMessage());
        }
    }
    if (ss == null) {
        toast("MCP 启动失败：无法绑定端口");
        return;
    }
    serverSock[0] = ss;
    serverOn[0] = true;
    String ip = getLocalIp();
    serverAddr[0] = ip;
    int lp = ss.getLocalPort();
    log("/Log/mcp.txt", "MCP 监听 http://" + ip + ":" + lp + "/mcp");
    toast("MCP 已启动 http://" + ip + ":" + lp + "/mcp");
    while (serverOn[0]) {
        Socket c = null;
        try {
            c = ss.accept();
            c.setSoTimeout(20000);
            synchronized (activeMcpConnections) {
                if (activeMcpConnections[0] >= 4) {
                    writeResp(c.getOutputStream(), 503, "application/json; charset=utf-8", "{\"error\":\"server_busy\",\"message\":\"并发请求已达上限，请稍后重试\"}");
                    continue;
                }
                activeMcpConnections[0]++;
            }
            final Socket client = c;
            try {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            handleConn(client);
                        } finally {
                            try { client.close(); } catch (Exception e) {}
                            synchronized (activeMcpConnections) {
                                activeMcpConnections[0] = Math.max(0, activeMcpConnections[0] - 1);
                            }
                        }
                    }
                }, "QFun-MCP-connection").start();
                c = null;
            } catch (Exception e) {
                synchronized (activeMcpConnections) {
                    activeMcpConnections[0] = Math.max(0, activeMcpConnections[0] - 1);
                }
                throw e;
            }
        } catch (Exception e) {
            if (serverOn[0]) log("/Log/mcp.txt", "连接异常: " + e.getMessage());
        } finally {
            try { if (c != null) c.close(); } catch (Exception e) {}
        }
    }
}

void handleConn(Socket c) {
    try {
        InputStream in = c.getInputStream();
        OutputStream out = c.getOutputStream();
        String head = readHead(in);
        if (head == null || head.length() == 0) {
            writeResp(out, 400, "application/json; charset=utf-8", "{\"error\":\"invalid_headers\"}");
            return;
        }
        String[] lines = head.split("\\r?\\n");
        if (lines.length == 0) return;
        String[] parts = lines[0].split(" ");
        String method = parts.length > 0 ? parts[0] : "";
        String path = parts.length > 1 ? parts[1] : "/";
        HashMap hdrs = new HashMap();
        for (int i = 1; i < lines.length; i++) {
            int idx = lines[i].indexOf(":");
            if (idx > 0) hdrs.put(lines[i].substring(0, idx).trim().toLowerCase(), lines[i].substring(idx + 1).trim());
        }
        if ("OPTIONS".equals(method)) { writeResp(out, 204, "", ""); return; }
        if ("GET".equals(method)) { writeResp(out, 405, "text/plain; charset=utf-8", "Method Not Allowed: use POST " + path); return; }
        if ("DELETE".equals(method)) { writeResp(out, 200, "application/json; charset=utf-8", "{}"); return; }
        if (!"POST".equals(method)) { writeResp(out, 405, "text/plain; charset=utf-8", "Method Not Allowed"); return; }

        String tok = getToken();
        if (tok == null || tok.length() == 0) {
            addCall("[auth]", "", false, 0, "Token 配置不可用，已拒绝请求");
            writeResp(out, 500, "application/json; charset=utf-8", "{\"error\":\"token_unavailable\"}");
            return;
        }
        if (!"none".equalsIgnoreCase(tok)) {
            String auth = (String) hdrs.get("authorization");
            String supplied = auth == null ? "" : auth.trim();
            if (supplied.length() < 7 || !supplied.regionMatches(true, 0, "Bearer ", 0, 7) || !tok.equals(supplied.substring(7).trim())) {
                addCall("[auth]", "", false, 0, "未授权请求被拒绝");
                writeResp(out, 401, "application/json; charset=utf-8", "{\"error\":\"unauthorized\"}");
                return;
            }
        }

        int len = 0;
        String lengthHeader = (String) hdrs.get("content-length");
        try {
            if (lengthHeader != null) len = Integer.parseInt(lengthHeader.trim());
        } catch (Exception e) {
            writeResp(out, 400, "application/json; charset=utf-8", "{\"error\":\"invalid_content_length\"}");
            return;
        }
        if (len <= 0) {
            writeResp(out, 400, "application/json; charset=utf-8", "{\"error\":\"empty_body\"}");
            return;
        }
        if (len > 1048576) {
            writeResp(out, 413, "application/json; charset=utf-8", "{\"error\":\"body_too_large\",\"max_bytes\":1048576}");
            return;
        }
        String body = readBody(in, len);
        if (body == null || body.getBytes("UTF-8").length != len) {
            writeResp(out, 400, "application/json; charset=utf-8", "{\"error\":\"incomplete_body\"}");
            return;
        }
        String resp = routeReq(body);
        if (resp == null) writeResp(out, 202, "", "");
        else writeResp(out, 200, "application/json; charset=utf-8", resp);
    } catch (Exception e) {
        log("/Log/mcp.txt", "handleConn: " + e.getMessage());
    }
}

String routeReq(String body) {
    try {
        String b = body == null ? "" : body.trim();
        if (b.length() == 0) return errResp(null, -32700, "Parse error: empty body").toString();
        if (b.startsWith("[")) {
            JSONArray arr = new JSONArray(b);
            JSONArray out = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject one = handleReq(arr.getJSONObject(i));
                if (one != null) out.put(one);
            }
            if (out.length() == 0) return null;
            return out.toString();
        }
        JSONObject res = handleReq(new JSONObject(b));
        if (res == null) return null;
        return res.toString();
    } catch (Exception e) {
        return errResp(null, -32700, "Parse error: " + e.getMessage()).toString();
    }
}

JSONObject handleReq(JSONObject req) {
    try {
        String method = req.optString("method", "");
        Object id = req.has("id") ? req.get("id") : null;
        if (id != null && id == JSONObject.NULL) id = null;
        if (method.startsWith("notifications/") || id == null) return null;
        JSONObject params = req.optJSONObject("params");
        if (params == null) params = new JSONObject();

        if ("initialize".equals(method)) return okResp(id, doInitialize(params));
        if ("ping".equals(method)) return okResp(id, new JSONObject());
        if ("tools/list".equals(method)) return okResp(id, listTools(true));
        if ("tools/call".equals(method)) return okResp(id, callTool(params));
        if ("resources/list".equals(method)) {
            JSONObject o = new JSONObject();
            o.put("resources", new JSONArray());
            return okResp(id, o);
        }
        if ("prompts/list".equals(method)) {
            JSONObject o = new JSONObject();
            o.put("prompts", new JSONArray());
            return okResp(id, o);
        }
        return errResp(id, -32601, "Method not found: " + method);
    } catch (Exception e) {
        return errResp(null, -32603, "Internal error: " + e.getMessage());
    }
}

JSONObject doInitialize(JSONObject params) {
    try {
        JSONObject o = new JSONObject();
        String pv = params.optString("protocolVersion", "");
        if (pv.length() == 0) pv = "2025-06-18";
        o.put("protocolVersion", pv);
        JSONObject caps = new JSONObject();
        JSONObject t = new JSONObject();
        t.put("listChanged", false);
        caps.put("tools", t);
        o.put("capabilities", caps);
        JSONObject si = new JSONObject();
        si.put("name", "qfun-mcp");
        si.put("version", "1.1.0");
        o.put("serverInfo", si);
        o.put("instructions", "QFun MCP: uin 为好友QQ号或群号; chat_type 1=私聊 2=群聊 100=陌生人; 文本支持 [atUin=QQ] 与 [pic=url或路径]; tools/call 尽可能返回 structuredContent; run_script 支持 timeout_seconds，脚本端上限默认30秒; 可用工具由手机端面板控制");
        return o;
    } catch (Exception e) {
        return new JSONObject();
    }
}

JSONObject okResp(Object id, JSONObject result) {
    JSONObject o = new JSONObject();
    try {
        o.put("jsonrpc", "2.0");
        o.put("id", id);
        o.put("result", result);
    } catch (Exception e) {}
    return o;
}

JSONObject errResp(Object id, int code, String msg) {
    JSONObject o = new JSONObject();
    try {
        o.put("jsonrpc", "2.0");
        o.put("id", id);
        JSONObject e = new JSONObject();
        e.put("code", code);
        e.put("message", msg);
        o.put("error", e);
    } catch (Exception ex) {}
    return o;
}

String readHead(InputStream in) {
    try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int same = 0;
        int b = in.read();
        while (b != -1) {
            if (bos.size() >= 16384) return null;
            bos.write(b);
            if (b == 10) {
                same++;
                if (same >= 2) break;
            } else if (b != 13) {
                same = 0;
            }
            b = in.read();
        }
        return new String(bos.toByteArray(), "UTF-8");
    } catch (Exception e) {
        return null;
    }
}

String readBody(InputStream in, int len) {
    try {
        byte[] buf = new byte[len];
        int got = 0;
        while (got < len) {
            int n = in.read(buf, got, len - got);
            if (n <= 0) break;
            got += n;
        }
        if (got != len) return null;
        return new String(buf, 0, got, "UTF-8");
    } catch (Exception e) {
        return null;
    }
}

void writeResp(OutputStream out, int code, String ctype, String body) {
    try {
        byte[] bb = body == null ? new byte[0] : body.getBytes("UTF-8");
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(code).append(" ").append(statusText(code)).append("\r\n");
        if (ctype != null && ctype.length() > 0) sb.append("Content-Type: ").append(ctype).append("\r\n");
        sb.append("Content-Length: ").append(bb.length).append("\r\n");
        sb.append("Access-Control-Allow-Origin: *\r\n");
        sb.append("Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS\r\n");
        sb.append("Access-Control-Allow-Headers: content-type, authorization, mcp-session-id, mcp-protocol-version, accept\r\n");
        sb.append("Access-Control-Expose-Headers: mcp-session-id\r\n");
        sb.append("Allow: GET, POST, DELETE, OPTIONS\r\n");
        sb.append("Connection: close\r\n");
        sb.append("\r\n");
        out.write(sb.toString().getBytes("UTF-8"));
        if (bb.length > 0) out.write(bb);
        out.flush();
    } catch (Exception e) {}
}

String statusText(int code) {
    if (code == 200) return "OK";
    if (code == 202) return "Accepted";
    if (code == 204) return "No Content";
    if (code == 400) return "Bad Request";
    if (code == 401) return "Unauthorized";
    if (code == 405) return "Method Not Allowed";
    if (code == 413) return "Payload Too Large";
    if (code == 500) return "Internal Server Error";
    if (code == 503) return "Service Unavailable";
    return "OK";
}