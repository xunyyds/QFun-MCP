boolean isToolEnabled(String name) {
    try {
        return getBoolean("mcp", "tool_" + name, false);
    } catch (Exception e) {
        return false;
    }
}

int totalToolCount() {
    try {
        return listTools(false).getJSONArray("tools").length();
    } catch (Exception e) {
        return 0;
    }
}

int enabledToolCount() {
    try {
        return listTools(true).getJSONArray("tools").length();
    } catch (Exception e) {
        return 0;
    }
}

String argStr(JSONObject a, String k, String d) {
    try {
        Object v = a.opt(k);
        if (v == null || v == JSONObject.NULL) return d;
        String s = String.valueOf(v).trim();
        return s.length() == 0 ? d : s;
    } catch (Exception e) {
        return d;
    }
}

long argLong(JSONObject a, String k, long d) {
    try {
        Object v = a.opt(k);
        if (v == null || v == JSONObject.NULL) return d;
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.parseLong(String.valueOf(v).trim());
    } catch (Exception e) {
        return d;
    }
}

int argInt(JSONObject a, String k, int d) {
    long v = argLong(a, k, d);
    return v < Integer.MIN_VALUE || v > Integer.MAX_VALUE ? d : (int) v;
}

boolean argBool(JSONObject a, String k, boolean d) {
    try {
        Object v = a.opt(k);
        if (v == null || v == JSONObject.NULL) return d;
        if (v instanceof Boolean) return ((Boolean) v).booleanValue();
        String s = String.valueOf(v).trim();
        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s) || "0".equals(s) || "no".equalsIgnoreCase(s)) return false;
        return d;
    } catch (Exception e) {
        return d;
    }
}

boolean isIntegralArg(JSONObject a, String key) {
    try {
        Object v = a.opt(key);
        if (v == null || v == JSONObject.NULL) return false;
        if (v instanceof Number) return ((Number) v).doubleValue() == ((Number) v).longValue();
        Long.parseLong(String.valueOf(v).trim());
        return true;
    } catch (Exception e) {
        return false;
    }
}

String[] requiredToolArgs(String name) {
    if ("send_msg".equals(name)) return new String[]{"uin", "content"};
    if ("send_pic".equals(name) || "send_ptt".equals(name) || "send_file".equals(name) || "send_video".equals(name)) return new String[]{"uin", "path"};
    if ("send_card".equals(name)) return new String[]{"uin", "json"};
    if ("reply_msg".equals(name)) return new String[]{"uin", "msg_id", "content"};
    if ("recall_msg".equals(name)) return new String[]{"uin", "msg_id"};
    if ("send_pai".equals(name)) return new String[]{"target_uin", "peer_uin"};
    if ("is_friend".equals(name) || "uin_to_uid".equals(name) || "send_zan".equals(name)) return new String[]{"uin"};
    if ("get_group_members".equals(name) || "get_prohibit_list".equals(name) || "get_group_info".equals(name) || "is_shut_up".equals(name) || "shut_up_all".equals(name) || "clock_in".equals(name)) return new String[]{"group_uin"};
    if ("get_member_info".equals(name)) return new String[]{"group_uin", "uin"};
    if ("shut_up".equals(name) || "kick".equals(name) || "set_admin".equals(name)) return new String[]{"group_uin", "uin"};
    if ("set_title".equals(name)) return new String[]{"group_uin", "uin", "title"};
    if ("change_card".equals(name)) return new String[]{"group_uin", "uin", "name"};
    if ("uid_to_uin".equals(name)) return new String[]{"uid"};
    if ("get_pskey".equals(name) || "get_pt4_token".equals(name) || "get_gtk".equals(name)) return new String[]{"domain"};
    if ("get_bkn".equals(name)) return new String[]{"key"};
    if ("run_script".equals(name)) return new String[]{"code"};
    return new String[0];
}

String validateToolArgs(String name, JSONObject a) {
    String[] required = requiredToolArgs(name);
    for (int i = 0; i < required.length; i++) {
        String key = required[i];
        if (!a.has(key) || a.isNull(key)) return "缺少必填参数: " + key;
        Object value = a.opt(key);
        if (!("title".equals(key) && "set_title".equals(name)) && String.valueOf(value).trim().length() == 0) return "参数不能为空: " + key;
    }
    String[] stringKeys = new String[]{"content", "path", "json", "uid", "domain", "key", "code", "title", "name"};
    for (int i = 0; i < stringKeys.length; i++) {
        String key = stringKeys[i];
        if (a.has(key) && !a.isNull(key) && !(a.opt(key) instanceof String)) return "参数必须是字符串: " + key;
    }
    String[] integerKeys = new String[]{"chat_type", "msg_id", "count", "seconds", "timeout_seconds"};
    for (int i = 0; i < integerKeys.length; i++) {
        String key = integerKeys[i];
        if (a.has(key) && !a.isNull(key) && !isIntegralArg(a, key)) return "参数必须是整数: " + key;
    }
    if (a.has("chat_type") && !a.isNull("chat_type")) {
        long type = argLong(a, "chat_type", -1);
        if (type != 1 && type != 2 && type != 100) return "chat_type 仅支持 1、2、100";
    }
    if (a.has("msg_id") && argLong(a, "msg_id", 0) <= 0) return "msg_id 必须大于 0";
    if (a.has("count") && argLong(a, "count", 0) < 1) return "count 必须大于 0";
    if (a.has("seconds") && argLong(a, "seconds", -1) < 0) return "seconds 不能小于 0";
    if (a.has("timeout_seconds") && argLong(a, "timeout_seconds", 0) < 1) return "timeout_seconds 必须大于 0";
    String[] boolKeys = new String[]{"enable", "black"};
    for (int i = 0; i < boolKeys.length; i++) {
        String key = boolKeys[i];
        if (a.has(key) && !a.isNull(key)) {
            Object value = a.opt(key);
            String text = String.valueOf(value);
            if (!(value instanceof Boolean) && !("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text) || "1".equals(text) || "0".equals(text) || "yes".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text))) return "参数必须是布尔值: " + key;
        }
    }
    return "";
}

Object normalizeResult(Object value, int depth) {
    try {
        if (value == null || value == JSONObject.NULL) return JSONObject.NULL;
        if (value instanceof String) return limitString((String) value, 4000);
        if (value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) return value;
        if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            return Double.isNaN(d) || Double.isInfinite(d) ? String.valueOf(value) : value;
        }
        if (value instanceof Character) return String.valueOf(value);
        if (depth >= 3) {
            JSONObject summary = new JSONObject();
            summary.put("_type", value.getClass().getName());
            summary.put("_value", limitString(String.valueOf(value), 1000));
            return summary;
        }
        if (value instanceof JSONObject) {
            JSONObject source = (JSONObject) value;
            JSONObject out = new JSONObject();
            java.util.Iterator keys = source.keys();
            int count = 0;
            while (keys.hasNext() && count < 60) {
                String key = String.valueOf(keys.next());
                out.put(key, normalizeResult(source.opt(key), depth + 1));
                count++;
            }
            if (keys.hasNext()) out.put("_truncated", true);
            return out;
        }
        if (value instanceof JSONArray) {
            JSONArray source = (JSONArray) value;
            JSONArray out = new JSONArray();
            int limit = Math.min(source.length(), 500);
            for (int i = 0; i < limit; i++) out.put(normalizeResult(source.opt(i), depth + 1));
            if (source.length() > limit) {
                JSONObject tail = new JSONObject();
                tail.put("_truncated", true);
                tail.put("omitted", source.length() - limit);
                out.put(tail);
            }
            return out;
        }
        if (value instanceof Map) {
            JSONObject out = new JSONObject();
            Map map = (Map) value;
            Iterator it = map.entrySet().iterator();
            int count = 0;
            while (it.hasNext() && count < 60) {
                Map.Entry entry = (Map.Entry) it.next();
                out.put(String.valueOf(entry.getKey()), normalizeResult(entry.getValue(), depth + 1));
                count++;
            }
            if (it.hasNext()) out.put("_truncated", true);
            return out;
        }
        if (value instanceof Iterable) {
            JSONArray out = new JSONArray();
            Iterator it = ((Iterable) value).iterator();
            int count = 0;
            while (it.hasNext() && count < 500) {
                out.put(normalizeResult(it.next(), depth + 1));
                count++;
            }
            if (it.hasNext()) {
                JSONObject tail = new JSONObject();
                tail.put("_truncated", true);
                out.put(tail);
            }
            return out;
        }
        if (value.getClass().isArray()) {
            JSONArray out = new JSONArray();
            int length = java.lang.reflect.Array.getLength(value);
            int limit = Math.min(length, 500);
            for (int i = 0; i < limit; i++) out.put(normalizeResult(java.lang.reflect.Array.get(value, i), depth + 1));
            if (length > limit) {
                JSONObject tail = new JSONObject();
                tail.put("_truncated", true);
                tail.put("omitted", length - limit);
                out.put(tail);
            }
            return out;
        }
        JSONObject out = new JSONObject();
        out.put("_type", value.getClass().getName());
        Field[] fields = value.getClass().getDeclaredFields();
        int count = 0;
        for (int i = 0; i < fields.length && count < 60; i++) {
            Field field = fields[i];
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
            try {
                field.setAccessible(true);
                out.put(field.getName(), normalizeResult(field.get(value), depth + 1));
                count++;
            } catch (Throwable e) {}
        }
        if (count == 0) out.put("_value", limitString(String.valueOf(value), 1000));
        if (fields.length > count) out.put("_fieldsLimited", true);
        return out;
    } catch (Throwable e) {
        JSONObject fallback = new JSONObject();
        try {
            fallback.put("_type", value == null ? "null" : value.getClass().getName());
            fallback.put("_value", limitString(String.valueOf(value), 1000));
        } catch (Exception ex) {}
        return fallback;
    }
}

String limitString(String value, int max) {
    if (value == null) return "";
    if (value.length() <= max) return value;
    return value.substring(0, max) + "…[截断，原长度 " + value.length() + "]";
}

JSONObject dumpObj(Object o) {
    if (o == null) return new JSONObject();
    Object normalized = normalizeResult(o, 0);
    if (normalized instanceof JSONObject) return (JSONObject) normalized;
    JSONObject result = new JSONObject();
    try { result.put("value", normalized); } catch (Exception e) {}
    return result;
}

JSONObject buildTool(String name, String desc, Object[] props) {
    JSONObject t = new JSONObject();
    try {
        t.put("name", name);
        t.put("description", desc);
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        JSONObject pj = new JSONObject();
        JSONArray req = new JSONArray();
        for (int i = 0; i < props.length; i++) {
            Object[] p = (Object[]) props[i];
            JSONObject pr = new JSONObject();
            pr.put("type", (String) p[1]);
            pr.put("description", (String) p[2]);
            if (p.length > 4 && p[4] instanceof JSONObject) {
                JSONObject rules = (JSONObject) p[4];
                java.util.Iterator ruleKeys = rules.keys();
                while (ruleKeys.hasNext()) {
                    String ruleKey = String.valueOf(ruleKeys.next());
                    pr.put(ruleKey, rules.opt(ruleKey));
                }
            }
            pj.put((String) p[0], pr);
            if ("req".equals(p[3])) req.put((String) p[0]);
        }
        schema.put("properties", pj);
        schema.put("additionalProperties", false);
        if (req.length() > 0) schema.put("required", req);
        t.put("inputSchema", schema);
    } catch (Exception e) {}
    return t;
}

JSONObject listTools(boolean onlyEnabled) {
    JSONObject o = new JSONObject();
    JSONArray ts = new JSONArray();
    try {
        ts.put(buildTool("send_msg", "发送文本消息内容支持 [atUin=QQ号]（0=全体成员）与 [pic=图片链接或本地路径] 组合", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"content", "string", "消息内容", "req"},
            new Object[]{"chat_type", "integer", "1=好友/私聊 2=群聊 100=陌生人，默认1", "opt"}
        }));
        ts.put(buildTool("send_pic", "发送图片", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "图片本地绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_ptt", "发送语音", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "语音文件绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_card", "发送 JSON 卡片消息", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"json", "string", "卡片 JSON 内容", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_file", "发送文件", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "文件绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_video", "发送视频", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"path", "string", "视频文件绝对路径", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("reply_msg", "引用回复某条消息", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"msg_id", "integer", "被引用消息的 msgId", "req"},
            new Object[]{"content", "string", "回复内容", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("recall_msg", "撤回一条消息", new Object[]{
            new Object[]{"uin", "string", "好友QQ号或群号", "req"},
            new Object[]{"msg_id", "integer", "要撤回的消息 msgId", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("send_pai", "拍一拍某人", new Object[]{
            new Object[]{"target_uin", "string", "被拍者QQ号", "req"},
            new Object[]{"peer_uin", "string", "群聊传群号，私聊传好友QQ", "req"},
            new Object[]{"chat_type", "integer", "聊天类型，默认1", "opt"}
        }));
        ts.put(buildTool("get_friends", "获取全部好友列表，返回 uin/uid/name/remark", new Object[]{}));
        ts.put(buildTool("is_friend", "判断某 QQ 号是否好友", new Object[]{
            new Object[]{"uin", "string", "QQ号", "req"}
        }));
        ts.put(buildTool("get_groups", "获取全部群列表，返回 group/name/owner", new Object[]{}));
        ts.put(buildTool("get_group_members", "获取群成员列表，返回 uin/name/role/joinGroupTime/lastActiveTime", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("get_member_info", "获取单个群成员资料", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"}
        }));
        ts.put(buildTool("get_prohibit_list", "获取群禁言列表，返回 user/userName/time/endTime", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("get_group_info", "获取群原始信息（TroopInfo 字段导出）", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("uin_to_uid", "QQ号(Uin) 转 Uid", new Object[]{
            new Object[]{"uin", "string", "QQ号", "req"}
        }));
        ts.put(buildTool("uid_to_uin", "Uid 转 QQ号(Uin)", new Object[]{
            new Object[]{"uid", "string", "Uid", "req"}
        }));
        ts.put(buildTool("send_zan", "给好友点赞", new Object[]{
            new Object[]{"uin", "string", "好友QQ号", "req"},
            new Object[]{"count", "integer", "点赞次数，默认1", "opt"}
        }));
        ts.put(buildTool("get_self_info", "获取当前登录 QQ 号与本服务器连接信息", new Object[]{}));
        ts.put(buildTool("shut_up", "禁言群成员，seconds=0 解除禁言", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"seconds", "integer", "禁言秒数，0=解禁，默认600", "opt"}
        }));
        ts.put(buildTool("shut_up_all", "开启/关闭全员禁言", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"enable", "boolean", "true=开启 false=关闭，默认true", "opt"}
        }));
        ts.put(buildTool("is_shut_up", "查询群是否处于全员禁言", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("kick", "踢出群成员", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"black", "boolean", "是否拉黑（拒绝再次入群），默认false", "opt"}
        }));
        ts.put(buildTool("set_admin", "设置/取消群管理员", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"enable", "boolean", "true=设为管理员 false=取消，默认true", "opt"}
        }));
        ts.put(buildTool("set_title", "设置成员群头衔（仅群主可用）", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"title", "string", "头衔文本，空串清除", "req"}
        }));
        ts.put(buildTool("change_card", "修改成员群名片", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"},
            new Object[]{"uin", "string", "成员QQ号", "req"},
            new Object[]{"name", "string", "新群名片", "req"}
        }));
        ts.put(buildTool("clock_in", "群打卡", new Object[]{
            new Object[]{"group_uin", "string", "群号", "req"}
        }));
        ts.put(buildTool("get_skey", "获取当前账号 skey", new Object[]{}));
        ts.put(buildTool("get_real_skey", "获取当前账号 realSkey", new Object[]{}));
        ts.put(buildTool("get_pskey", "获取指定域名 pskey", new Object[]{
            new Object[]{"domain", "string", "域名，如 qzone.qq.com", "req"}
        }));
        ts.put(buildTool("get_pt4_token", "获取指定域名 pt4Token", new Object[]{
            new Object[]{"domain", "string", "域名", "req"}
        }));
        ts.put(buildTool("get_stweb", "获取 stweb 凭证", new Object[]{}));
        ts.put(buildTool("get_gtk", "获取指定域名 GTK 值", new Object[]{
            new Object[]{"domain", "string", "域名", "req"}
        }));
        ts.put(buildTool("get_group_rkey", "获取群聊图片 RKey", new Object[]{}));
        ts.put(buildTool("get_friend_rkey", "获取私聊图片 RKey", new Object[]{}));
        ts.put(buildTool("get_bkn", "计算 BKN", new Object[]{
            new Object[]{"key", "string", "参与计算的 key", "req"}
        }));
        JSONObject timeoutRules = new JSONObject();
        timeoutRules.put("minimum", 1);
        timeoutRules.put("maximum", Math.max(1, runScriptMaxTimeoutSeconds));
        timeoutRules.put("default", Math.max(1, runScriptMaxTimeoutSeconds));
        ts.put(buildTool("run_script", "【高风险】执行 BeanShell 脚本并返回结构化结果、结果类型、耗时及异常详情。timeout_seconds 可指定等待秒数，最终上限由脚本端 runScriptMaxTimeoutSeconds 控制，默认30秒；超时后会尝试中断，但不能保证任意脚本立即停止。建议默认禁用", new Object[]{
            new Object[]{"code", "string", "BeanShell 代码；返回结果会结构化回传", "req"},
            new Object[]{"timeout_seconds", "integer", "本次希望等待的秒数，默认使用脚本端上限；最小1秒，超过上限时会被限制", "opt", timeoutRules}
        }));

        JSONArray filtered = new JSONArray();
        for (int i = 0; i < ts.length(); i++) {
            JSONObject t = ts.getJSONObject(i);
            if (!onlyEnabled || isToolEnabled(t.optString("name", ""))) filtered.put(t);
        }
        o.put("tools", filtered);
    } catch (Exception e) {
        log("/Log/mcp.txt", "listTools 失败: " + e.getMessage());
    }
    return o;
}

void addCall(String name, String args, boolean ok, long ms, String text) {
    try {
        String a = args == null ? "" : args;
        if (a.length() > 100) a = a.substring(0, 100) + "..";
        String time = new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date());
        String line = time + " | " + name + (a.length() > 0 ? " " + a : "") + " | " + (ok ? "ok" : "失败") + " " + ms + "ms";
        if (!ok && text != null && text.length() > 0) line = line + " | " + limitString(text, 240);
        synchronized (callLog) {
            callLog.add(0, line);
            while (callLog.size() > 200) callLog.remove(callLog.size() - 1);
        }
        log("/Log/mcp_calls.txt", line);
    } catch (Exception e) {}
}

JSONObject callTool(JSONObject params) {
    JSONObject res = new JSONObject();
    try {
        String name = params.optString("name", "");
        JSONObject args = params.optJSONObject("arguments");
        if (args == null) args = new JSONObject();
        long t0 = System.currentTimeMillis();
        JSONObject r = invokeTool(name, args);
        long ms = System.currentTimeMillis() - t0;
        boolean isError = r.optBoolean("isError", false);
        String text = r.optString("text", "");
        addCall(name, args.toString(), !isError, ms, text);
        JSONArray content = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("type", "text");
        item.put("text", text);
        content.put(item);
        res.put("content", content);
        res.put("isError", isError);
        if (r.has("data")) {
            Object data = r.opt("data");
            if (data instanceof JSONObject) {
                JSONObject structured = (JSONObject) data;
                if (r.optBoolean("actionResult", false) || isError) structured.put("tool", name);
                res.put("structuredContent", structured);
            } else {
                JSONObject structured = new JSONObject();
                structured.put("result", data == null ? JSONObject.NULL : data);
                structured.put("tool", name);
                res.put("structuredContent", structured);
            }
        }
    } catch (Exception e) {
        try {
            JSONArray content = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("type", "text");
            item.put("text", "调用失败: " + e.getClass().getName() + ": " + String.valueOf(e.getMessage()));
            content.put(item);
            res.put("content", content);
            res.put("isError", true);
            JSONObject detail = new JSONObject();
            detail.put("success", false);
            detail.put("error_type", e.getClass().getName());
            detail.put("error", String.valueOf(e.getMessage()));
            res.put("structuredContent", detail);
        } catch (Exception ex) {}
    }
    return res;
}

void setStructured(JSONObject r, Object value, boolean isError) {
    try {
        Object normalized = normalizeResult(value, 0);
        String encoded = normalized == null ? "null" : String.valueOf(normalized);
        if (encoded.length() > 24000) {
            JSONObject clipped = new JSONObject();
            clipped.put("truncated", true);
            clipped.put("original_length", encoded.length());
            clipped.put("preview", encoded.substring(0, 20000));
            clipped.put("message", "结果过长，已截断预览");
            normalized = clipped;
            encoded = clipped.toString();
        }
        r.put("data", normalized);
        r.put("text", encoded);
        r.put("isError", isError);
    } catch (Exception e) {
        try {
            r.put("text", "结果序列化失败: " + e.getClass().getName() + ": " + e.getMessage());
            r.put("isError", true);
        } catch (Exception ex) {}
    }
}

void setOk(JSONObject r, String name, JSONObject args) {
    try {
        JSONObject data = new JSONObject();
        JSONObject used = new JSONObject();
        String[] keys = new String[]{"uin", "group_uin", "target_uin", "peer_uin", "chat_type", "msg_id", "seconds", "enable", "black", "count", "title", "name", "path"};
        for (int i = 0; i < keys.length; i++) {
            if (args.has(keys[i]) && !args.isNull(keys[i])) used.put(keys[i], args.opt(keys[i]));
        }
        data.put("success", true);
        data.put("operation", name);
        data.put("parameters", used);
        data.put("message", "调用已完成；宿主 API 未提供可验证的操作回执");
        r.put("actionResult", true);
        setStructured(r, data, false);
        r.put("text", "操作调用已完成；宿主 API 未提供可验证的操作回执");
    } catch (Exception e) {}
}

void setJson(JSONObject r, Object v) {
    setStructured(r, v, false);
}

void setFail(JSONObject r, String msg) {
    try {
        JSONObject data = new JSONObject();
        data.put("success", false);
        data.put("error", msg == null ? "未知错误" : msg);
        setStructured(r, data, true);
        r.put("text", msg == null ? "未知错误" : msg);
    } catch (Exception e) {
        try { r.put("text", msg == null ? "未知错误" : msg); r.put("isError", true); } catch (Exception ex) {}
    }
}

void addExceptionDetails(JSONObject out, Throwable error) {
    if (out == null || error == null) return;
    try {
        out.put("error_type", error.getClass().getName());
        out.put("error", String.valueOf(error.getMessage()));
        JSONArray stack = new JSONArray();
        StackTraceElement[] elements = error.getStackTrace();
        for (int i = 0; i < elements.length && i < 16; i++) stack.put(String.valueOf(elements[i]));
        out.put("stack_trace", stack);
        Throwable cause = error.getCause();
        if (cause != null) {
            JSONObject causeInfo = new JSONObject();
            causeInfo.put("type", cause.getClass().getName());
            causeInfo.put("message", String.valueOf(cause.getMessage()));
            out.put("cause", causeInfo);
        }
    } catch (Exception e) {}
}

JSONObject runScriptResult(String status, String message, long startedAt, int requestedTimeout, int effectiveTimeout, Object value, Throwable error, boolean interrupted, boolean alive) {
    JSONObject out = new JSONObject();
    try {
        out.put("success", "success".equals(status));
        out.put("status", status);
        out.put("message", message == null ? "" : message);
        out.put("duration_ms", System.currentTimeMillis() - startedAt);
        out.put("requested_timeout_seconds", requestedTimeout);
        out.put("effective_timeout_seconds", effectiveTimeout);
        out.put("max_timeout_seconds", Math.max(1, runScriptMaxTimeoutSeconds));
        if (value != null || "success".equals(status)) {
            out.put("result_type", value == null ? "null" : value.getClass().getName());
            out.put("result", normalizeResult(value, 0));
            out.put("result_text", limitString(value == null ? "null" : String.valueOf(value), 4000));
        }
        if (error != null) addExceptionDetails(out, error);
        if (interrupted) out.put("interrupt_requested", true);
        if (alive) out.put("thread_alive_after_timeout", true);
    } catch (Exception e) {}
    return out;
}

JSONObject invokeTool(String name, JSONObject a) {
    JSONObject r = new JSONObject();
    try {
        if (!isToolEnabled(name)) {
            setFail(r, "工具已禁用，请在手机端 MCP 面板启用");
            return r;
        }
        String validationError = validateToolArgs(name, a);
        if (validationError.length() > 0) {
            setFail(r, validationError);
            return r;
        }
        if ("send_msg".equals(name)) {
            sendMsg(argStr(a, "uin", ""), argStr(a, "content", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("send_pic".equals(name)) {
            sendPic(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("send_ptt".equals(name)) {
            sendPtt(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("send_card".equals(name)) {
            sendCard(argStr(a, "uin", ""), argStr(a, "json", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("send_file".equals(name)) {
            sendFile(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("send_video".equals(name)) {
            sendVideo(argStr(a, "uin", ""), argStr(a, "path", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("reply_msg".equals(name)) {
            sendReplyMsg(argStr(a, "uin", ""), argLong(a, "msg_id", 0), argStr(a, "content", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("recall_msg".equals(name)) {
            recallMsg(argInt(a, "chat_type", 1), argStr(a, "uin", ""), argLong(a, "msg_id", 0));
            setOk(r, name, a);
        } else if ("send_pai".equals(name)) {
            sendPai(argStr(a, "target_uin", ""), argStr(a, "peer_uin", ""), argInt(a, "chat_type", 1));
            setOk(r, name, a);
        } else if ("get_friends".equals(name)) {
            List fs = getAllFriend();
            JSONArray arr = new JSONArray();
            for (int i = 0; i < fs.size(); i++) {
                Object f = fs.get(i);
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(f.uin));
                o.put("uid", String.valueOf(f.uid));
                o.put("name", String.valueOf(f.name));
                o.put("remark", String.valueOf(f.remark));
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("is_friend".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("isFriend", isFriend(argStr(a, "uin", "")));
            setJson(r, o);
        } else if ("get_groups".equals(name)) {
            List gs = getGroupList();
            JSONArray arr = new JSONArray();
            for (int i = 0; i < gs.size(); i++) {
                Object g = gs.get(i);
                JSONObject o = new JSONObject();
                o.put("group", String.valueOf(g.group));
                o.put("name", String.valueOf(g.groupName));
                o.put("owner", String.valueOf(g.groupOwner));
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("get_group_members".equals(name)) {
            List ms = getGroupMemberList(argStr(a, "group_uin", ""));
            JSONArray arr = new JSONArray();
            for (int i = 0; i < ms.size(); i++) {
                Object m = ms.get(i);
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(m.uin));
                o.put("name", String.valueOf(m.uinName));
                o.put("role", String.valueOf(m.role));
                o.put("joinGroupTime", m.joinGroupTime);
                o.put("lastActiveTime", m.lastActiveTime);
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("get_member_info".equals(name)) {
            Object mi = getMemberInfo(argStr(a, "group_uin", ""), argStr(a, "uin", ""));
            if (mi == null) setFail(r, "未找到该成员");
            else {
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(mi.uin));
                o.put("name", String.valueOf(mi.uinName));
                o.put("role", String.valueOf(mi.role));
                o.put("joinGroupTime", mi.joinGroupTime);
                o.put("lastActiveTime", mi.lastActiveTime);
                setJson(r, o);
            }
        } else if ("get_prohibit_list".equals(name)) {
            List ps = getProhibitList(argStr(a, "group_uin", ""));
            JSONArray arr = new JSONArray();
            for (int i = 0; i < ps.size(); i++) {
                Object p = ps.get(i);
                JSONObject o = new JSONObject();
                o.put("uin", String.valueOf(p.user));
                o.put("name", String.valueOf(p.userName));
                o.put("remainSeconds", p.time);
                o.put("endTime", p.endTime);
                arr.put(o);
            }
            setJson(r, arr);
        } else if ("get_group_info".equals(name)) {
            Object gi = getGroupInfo(argStr(a, "group_uin", ""));
            setJson(r, dumpObj(gi));
        } else if ("uin_to_uid".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("uid", getUidFromUin(argStr(a, "uin", "")));
            setJson(r, o);
        } else if ("uid_to_uin".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("uin", getUinFromUid(argStr(a, "uid", "")));
            setJson(r, o);
        } else if ("send_zan".equals(name)) {
            sendZan(argStr(a, "uin", ""), argInt(a, "count", 1));
            setOk(r, name, a);
        } else if ("get_self_info".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("uin", myUin);
            String ip = serverAddr[0];
            if (ip == null || ip.length() == 0) ip = getLocalIp();
            o.put("server", "http://" + ip + ":" + mcpNowPort() + "/mcp");
            setJson(r, o);
        } else if ("shut_up".equals(name)) {
            shutUp(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argLong(a, "seconds", 600));
            setOk(r, name, a);
        } else if ("shut_up_all".equals(name)) {
            shutUpAll(argStr(a, "group_uin", ""), argBool(a, "enable", true));
            setOk(r, name, a);
        } else if ("is_shut_up".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("isShutUp", isShutUp(argStr(a, "group_uin", "")));
            setJson(r, o);
        } else if ("kick".equals(name)) {
            kickGroup(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argBool(a, "black", false));
            setOk(r, name, a);
        } else if ("set_admin".equals(name)) {
            setGroupAdmin(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argBool(a, "enable", true));
            setOk(r, name, a);
        } else if ("set_title".equals(name)) {
            setGroupMemberTitle(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argStr(a, "title", ""));
            setOk(r, name, a);
        } else if ("change_card".equals(name)) {
            changeMemberName(argStr(a, "group_uin", ""), argStr(a, "uin", ""), argStr(a, "name", ""));
            setOk(r, name, a);
        } else if ("clock_in".equals(name)) {
            clockIn(argStr(a, "group_uin", ""));
            setOk(r, name, a);
        } else if ("get_skey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("skey", getSkey());
            setJson(r, o);
        } else if ("get_real_skey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("realSkey", getRealSkey());
            setJson(r, o);
        } else if ("get_pskey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("pskey", getPskey(argStr(a, "domain", "")));
            setJson(r, o);
        } else if ("get_pt4_token".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("pt4Token", getPt4Token(argStr(a, "domain", "")));
            setJson(r, o);
        } else if ("get_stweb".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("stweb", getStweb());
            setJson(r, o);
        } else if ("get_gtk".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("gtk", String.valueOf(getGTK(argStr(a, "domain", ""))));
            setJson(r, o);
        } else if ("get_group_rkey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("rkey", getGroupRKey());
            setJson(r, o);
        } else if ("get_friend_rkey".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("rkey", getFriendRKey());
            setJson(r, o);
        } else if ("get_bkn".equals(name)) {
            JSONObject o = new JSONObject();
            o.put("bkn", String.valueOf(getBkn(argStr(a, "key", ""))));
            setJson(r, o);
        } else if ("run_script".equals(name)) {
            final String code = argStr(a, "code", "");
            final int maxTimeout = Math.max(1, runScriptMaxTimeoutSeconds);
            int requestedTimeout = a.has("timeout_seconds") ? argInt(a, "timeout_seconds", maxTimeout) : maxTimeout;
            if (requestedTimeout < 1) { setFail(r, "timeout_seconds 必须大于 0"); return r; }
            final int effectiveTimeout = Math.min(requestedTimeout, maxTimeout);
            boolean previousScriptRunning;
            synchronized (runScriptBusy) {
                previousScriptRunning = runScriptBusy[0];
                runScriptBusy[0] = true;
            }
            final long startedAt = System.currentTimeMillis();
            try {
                log("/Log/mcp_script.txt", "\n=== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " === timeout=" + effectiveTimeout + "s\n" + code + "\n");
            } catch (Exception e) {}
            final Object[] scriptResult = new Object[1];
            final Throwable[] scriptError = new Throwable[1];
            Thread worker = new Thread(new Runnable() {
                public void run() {
                    try {
                        scriptResult[0] = this.interpreter.eval(code);
                    } catch (Throwable e) {
                        scriptError[0] = e;
                    } finally {
                        synchronized (runScriptBusy) {
                            runScriptBusy[0] = false;
                        }
                    }
                }
            }, "QFun-MCP-run-script");
            worker.setDaemon(true);
            try {
                worker.start();
                worker.join(effectiveTimeout * 1000L);
            } catch (InterruptedException e) {
                worker.interrupt();
                Thread.currentThread().interrupt();
                setStructured(r, runScriptResult("interrupted", "等待执行结果的线程被中断；脚本线程已收到中断请求", startedAt, requestedTimeout, effectiveTimeout, null, e, true, worker.isAlive()), true);
                return r;
            } catch (Throwable e) {
                synchronized (runScriptBusy) { runScriptBusy[0] = false; }
                setStructured(r, runScriptResult("error", "无法启动脚本线程", startedAt, requestedTimeout, effectiveTimeout, null, e, false, false), true);
                return r;
            }
            if (worker.isAlive()) {
                worker.interrupt();
                setStructured(r, runScriptResult("timeout", (previousScriptRunning ? "启动时检测到上一个脚本仍在运行；" : "") + "执行超过有效超时时间，已请求中断。若脚本不响应中断，线程会继续运行；本次调用已返回，后续 run_script 仍可执行", startedAt, requestedTimeout, effectiveTimeout, null, null, true, true), true);
            } else if (scriptError[0] != null) {
                setStructured(r, runScriptResult("error", "脚本执行异常", startedAt, requestedTimeout, effectiveTimeout, null, scriptError[0], false, false), true);
            } else {
                setStructured(r, runScriptResult("success", (previousScriptRunning ? "启动时检测到上一个脚本仍在运行；" : "") + "脚本执行完成", startedAt, requestedTimeout, effectiveTimeout, scriptResult[0], null, false, false), false);
            }
        } else {
            setFail(r, "未知工具: " + name);
        }
    } catch (Throwable e) {
        JSONObject detail = new JSONObject();
        try {
            detail.put("success", false);
            detail.put("status", "error");
            detail.put("message", "工具执行异常");
            detail.put("tool", name);
            addExceptionDetails(detail, e);
            setStructured(r, detail, true);
        } catch (Exception ex) {
            setFail(r, "执行失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    return r;
}