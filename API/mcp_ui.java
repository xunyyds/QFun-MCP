int bgC, cardC, cardC2, textC, subC, accentC, borderC, okC, errC, warnC, rippleC;
ArrayList checkBoxes = new ArrayList();
ArrayList toolNames = new ArrayList();
HashMap catCheckMap = new HashMap();
HashMap catItemsMap = new HashMap();
HashMap catExpanded = new HashMap();
LinearLayout cfgBox, logBox;
ScrollView cfgScroll, logScroll;
Button tabA, tabB;
Dialog panelDlg;
TextView statusCntTv, cfgCountTv;

int px(int dp) {
    try {
        float d = context.getResources().getDisplayMetrics().density;
        return (int)(dp * d + 0.5f);
    } catch (Exception e) { return dp; }
}

int mcpNowPort() {
    try {
        if (serverSock[0] != null && !serverSock[0].isClosed()) return serverSock[0].getLocalPort();
    } catch (Exception e) {}
    return getPort();
}

void applyTheme() {
    boolean dark = false;
    try { dark = ThemeHelper.INSTANCE.isNightMode(); } catch (Exception e) {}
    if (dark) {
        bgC = Color.parseColor("#FF0F1318");
        cardC = Color.parseColor("#FF1B2028");
        cardC2 = Color.parseColor("#FF252B34");
        textC = Color.parseColor("#FFECEFF1");
        subC = Color.parseColor("#FF8B949E");
        accentC = Color.parseColor("#FF5AA9F5");
        borderC = Color.parseColor("#2AFFFFFF");
        okC = Color.parseColor("#FF4CAF50");
        errC = Color.parseColor("#FFEF5350");
        warnC = Color.parseColor("#FFFFA726");
        rippleC = Color.parseColor("#33FFFFFF");
    } else {
        bgC = Color.parseColor("#FFEDF1F6");
        cardC = Color.parseColor("#FFFFFFFF");
        cardC2 = Color.parseColor("#FFF3F6FA");
        textC = Color.parseColor("#FF1A2027");
        subC = Color.parseColor("#FF6B7785");
        accentC = Color.parseColor("#FF1976D2");
        borderC = Color.parseColor("#14000000");
        okC = Color.parseColor("#FF2E7D32");
        errC = Color.parseColor("#FFD32F2F");
        warnC = Color.parseColor("#FFEF6C00");
        rippleC = Color.parseColor("#1F000000");
    }
}

GradientDrawable round(int color, int radiusDp) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color);
    d.setCornerRadius(px(radiusDp));
    return d;
}

GradientDrawable roundB(int fillColor, int radiusDp, int borderColor, int borderDp) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(fillColor);
    d.setCornerRadius(px(radiusDp));
    if (borderDp > 0) d.setStroke(px(borderDp), borderColor);
    return d;
}

Drawable rippleRound(int fillColor, int radiusDp) {
    GradientDrawable content = new GradientDrawable();
    content.setColor(fillColor);
    content.setCornerRadius(px(radiusDp));
    GradientDrawable mask = new GradientDrawable();
    mask.setColor(Color.WHITE);
    mask.setCornerRadius(px(radiusDp));
    return new RippleDrawable(ColorStateList.valueOf(rippleC), content, mask);
}

Drawable rippleRoundB(int fillColor, int radiusDp, int borderColor, int borderDp) {
    GradientDrawable content = new GradientDrawable();
    content.setColor(fillColor);
    content.setCornerRadius(px(radiusDp));
    if (borderDp > 0) content.setStroke(px(borderDp), borderColor);
    GradientDrawable mask = new GradientDrawable();
    mask.setColor(Color.WHITE);
    mask.setCornerRadius(px(radiusDp));
    return new RippleDrawable(ColorStateList.valueOf(rippleC), content, mask);
}

TextView tv(String s, int color, int sp) {
    TextView t = new TextView(context);
    t.setText(s);
    t.setTextColor(color);
    t.setTextSize(sp);
    return t;
}

TextView tvBold(String s, int color, int sp) {
    TextView t = tv(s, color, sp);
    t.setTypeface(null, Typeface.BOLD);
    return t;
}

LinearLayout card() {
    LinearLayout box = new LinearLayout(context);
    box.setOrientation(LinearLayout.VERTICAL);
    box.setBackground(roundB(cardC, 16, borderC, 1));
    box.setPadding(px(14), px(12), px(14), px(12));
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    lp.setMargins(0, px(8), 0, 0);
    box.setLayoutParams(lp);
    return box;
}

Button btn(String s) {
    Button b = new Button(context);
    b.setAllCaps(false);
    b.setText(s);
    b.setTextColor(textC);
    b.setBackground(rippleRoundB(cardC2, 12, borderC, 1));
    b.setTextSize(13);
    b.setPadding(px(14), px(6), px(14), px(6));
    b.setMinHeight(px(38));
    b.setMinWidth(0);
    b.setMinimumWidth(0);
    try { b.setStateListAnimator(null); } catch (Exception e) {}
    return b;
}

Button btnPrimary(String s) {
    Button b = btn(s);
    b.setTextColor(Color.WHITE);
    b.setBackground(rippleRound(accentC, 12));
    return b;
}

Button btnSmall(String s) {
    Button b = btn(s);
    b.setTextSize(12);
    b.setPadding(px(8), px(2), px(8), px(2));
    b.setMinHeight(px(30));
    return b;
}

void copyText(String s) {
    try {
        Activity a = getNowActivity();
        if (a == null) { toast("复制失败：无前台界面"); return; }
        ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("mcp", s));
        toast("已复制");
    } catch (Exception e) { toast("复制失败: " + e.getMessage()); }
}

void switchTab(boolean showLog) {
    try {
        cfgScroll.setVisibility(showLog ? View.GONE : View.VISIBLE);
        logScroll.setVisibility(showLog ? View.VISIBLE : View.GONE);
        if (showLog) {
            tabA.setBackground(rippleRoundB(cardC2, 12, 0, 0));
            tabA.setTextColor(subC);
            tabB.setBackground(rippleRound(accentC, 12));
            tabB.setTextColor(Color.WHITE);
        } else {
            tabA.setBackground(rippleRound(accentC, 12));
            tabA.setTextColor(Color.WHITE);
            tabB.setBackground(rippleRoundB(cardC2, 12, 0, 0));
            tabB.setTextColor(subC);
        }
    } catch (Exception e) {}
}

String toolCat(String name) {
    if (name.startsWith("send_") || "reply_msg".equals(name) || "recall_msg".equals(name)) return "消息";
    if ("shut_up".equals(name) || "shut_up_all".equals(name) || "is_shut_up".equals(name)
        || "kick".equals(name) || "set_admin".equals(name) || "set_title".equals(name)
        || "change_card".equals(name) || "clock_in".equals(name)) return "群管理";
    if (name.startsWith("get_") || "is_friend".equals(name)
        || "uin_to_uid".equals(name) || "uid_to_uin".equals(name) || "send_zan".equals(name)) return "信息";
    if ("run_script".equals(name)) return "系统";
    return "凭证";
}

void setAll(boolean on) {
    for (int i = 0; i < checkBoxes.size(); i++) {
        try {
            CheckBox cb = (CheckBox) checkBoxes.get(i);
            String n = (String) toolNames.get(i);
            putBoolean("mcp", "tool_" + n, on);
            cb.setChecked(on);
        } catch (Exception e) {}
    }
    updateCount();
}

void updateCount() {
    try {
        int total = totalToolCount();
        int on = enabledToolCount();
        if (cfgCountTv != null) cfgCountTv.setText("已启用 " + on + " / " + total);
        if (statusCntTv != null) statusCntTv.setText(on + " / " + total + " 工具");
    } catch (Exception e) {}
}

CompoundButton.OnCheckedChangeListener makeToolListener(final String n) {
    return new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton bv, boolean isChecked) {
            putBoolean("mcp", "tool_" + n, isChecked);
            updateCount();
            refreshCatChecks();
        }
    };
}

void refreshCatChecks() {
    try {
        java.util.Iterator it = catCheckMap.keySet().iterator();
        while (it.hasNext()) {
            String cat = (String) it.next();
            CheckBox cb = (CheckBox) catCheckMap.get(cat);
            ArrayList items = (ArrayList) catItemsMap.get(cat);
            if (cb != null && items != null) {
                int on = 0;
                for (int i = 0; i < items.size(); i++) {
                    if (isToolEnabled(((JSONObject) items.get(i)).optString("name", ""))) on++;
                }
                cb.setOnCheckedChangeListener(null);
                cb.setChecked(on == items.size() && items.size() > 0);
                cb.setOnCheckedChangeListener(null);
            }
        }
    } catch (Exception e) {}
}

void setCat(ArrayList items, boolean on) {
    for (int i = 0; i < items.size(); i++) {
        try {
            String n = ((JSONObject) items.get(i)).optString("name", "");
            putBoolean("mcp", "tool_" + n, on);
            for (int k = 0; k < toolNames.size(); k++) {
                if (n.equals(toolNames.get(k))) {
                    CheckBox cb = (CheckBox) checkBoxes.get(k);
                    cb.setOnCheckedChangeListener(null);
                    cb.setChecked(on);
                    cb.setOnCheckedChangeListener(makeToolListener(n));
                    break;
                }
            }
        } catch (Exception e) {}
    }
    updateCount();
}

LinearLayout toolRow(JSONObject t) {
    final String n = t.optString("name", "");
    String desc = t.optString("description", "");

    LinearLayout row = new LinearLayout(context);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(px(4), px(6), px(4), px(6));

    LinearLayout textBox = new LinearLayout(context);
    textBox.setOrientation(LinearLayout.VERTICAL);
    textBox.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView nameTv = tv(n, textC, 14);
    nameTv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    textBox.addView(nameTv);

    if (desc != null && desc.length() > 0) {
        TextView descTv = tv(desc, subC, 11);
        descTv.setPadding(0, px(2), 0, 0);
        descTv.setMaxLines(3);
        descTv.setEllipsize(TextUtils.TruncateAt.END);
        textBox.addView(descTv);
    }
    row.addView(textBox);

    final CheckBox cb = new CheckBox(context);
    cb.setChecked(isToolEnabled(n));
    cb.setButtonTintList(ColorStateList.valueOf(accentC));
    cb.setOnCheckedChangeListener(makeToolListener(n));
    checkBoxes.add(cb);
    toolNames.add(n);
    row.addView(cb);
    return row;
}

LinearLayout catCard(final String cat, final ArrayList items) {
    LinearLayout box = card();

    LinearLayout head = new LinearLayout(context);
    head.setOrientation(LinearLayout.HORIZONTAL);
    head.setGravity(Gravity.CENTER_VERTICAL);
    head.setPadding(px(2), px(4), px(2), px(4));
    head.setBackground(rippleRound(cardC, 8));

    final TextView arrow = tv("", accentC, 16);
    arrow.setGravity(Gravity.CENTER);
    arrow.setLayoutParams(new LinearLayout.LayoutParams(px(20), px(20)));

    TextView title = tvBold(cat, textC, 15);
    TextView cnt = tv(items.size() + " 个", subC, 12);

    head.addView(arrow);
    LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    titleLp.setMargins(px(8), 0, 0, 0);
    head.addView(title, titleLp);

    LinearLayout.LayoutParams cntLp = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    cntLp.setMargins(0, 0, px(8), 0);
    head.addView(cnt, cntLp);

    final CheckBox catCb = new CheckBox(context);
    catCb.setButtonTintList(ColorStateList.valueOf(accentC));
    int onCount = 0;
    for (int i = 0; i < items.size(); i++) {
        if (isToolEnabled(((JSONObject) items.get(i)).optString("name", ""))) onCount++;
    }
    catCb.setChecked(onCount == items.size() && items.size() > 0);
    catCheckMap.put(cat, catCb);
    catItemsMap.put(cat, items);
    catCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton bv, boolean isChecked) {
            setCat(items, isChecked);
        }
    });
    head.addView(catCb);
    box.addView(head);

    final LinearLayout body = new LinearLayout(context);
    body.setOrientation(LinearLayout.VERTICAL);
    body.setPadding(0, px(6), 0, 0);

    Object expObj = catExpanded.get(cat);
    boolean exp = expObj == null ? true : ((Boolean) expObj).booleanValue();
    body.setVisibility(exp ? View.VISIBLE : View.GONE);
    arrow.setText(exp ? "▾" : "▸");

    for (int i = 0; i < items.size(); i++) {
        body.addView(toolRow((JSONObject) items.get(i)));
    }
    box.addView(body);

    head.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            boolean now = body.getVisibility() == View.VISIBLE;
            body.setVisibility(now ? View.GONE : View.VISIBLE);
            arrow.setText(now ? "▸" : "▾");
            catExpanded.put(cat, Boolean.valueOf(!now));
        }
    });
    return box;
}

void buildCfg(LinearLayout box) {
    box.removeAllViews();
    checkBoxes.clear();
    toolNames.clear();
    catCheckMap.clear();
    catItemsMap.clear();
    try {
        JSONArray arr = listTools(false).getJSONArray("tools");
        HashMap groups = new HashMap();
        String[] order = {"消息", "信息", "群管理", "凭证", "系统"};
        for (int i = 0; i < order.length; i++) groups.put(order[i], new ArrayList());

        for (int i = 0; i < arr.length(); i++) {
            JSONObject t = arr.getJSONObject(i);
            String cat = toolCat(t.optString("name", ""));
            if (!groups.containsKey(cat)) groups.put(cat, new ArrayList());
            ((ArrayList) groups.get(cat)).add(t);
        }

        for (int i = 0; i < order.length; i++) {
            ArrayList items = (ArrayList) groups.get(order[i]);
            if (items == null || items.size() == 0) continue;
            box.addView(catCard(order[i], items));
        }
        updateCount();
    } catch (Exception e) {
        box.addView(tv("配置加载失败: " + e.getMessage(), subC, 13));
    }
}

LinearLayout logCard(String line) {
    LinearLayout box = card();
    box.setPadding(px(12), px(10), px(12), px(10));

    int badgeColor = subC;
    String badgeText = "?";
    if (line.contains("| ok ")) { badgeColor = okC; badgeText = "OK"; }
    else if (line.contains("| 失败 ")) { badgeColor = errC; badgeText = "FAIL"; }
    else if (line.startsWith("[auth]")) { badgeColor = warnC; badgeText = "AUTH"; }

    LinearLayout head = new LinearLayout(context);
    head.setOrientation(LinearLayout.HORIZONTAL);
    head.setGravity(Gravity.CENTER_VERTICAL);

    View dot = new View(context);
    GradientDrawable dotBg = new GradientDrawable();
    dotBg.setShape(GradientDrawable.OVAL);
    dotBg.setColor(badgeColor);
    dot.setBackground(dotBg);
    head.addView(dot, new LinearLayout.LayoutParams(px(8), px(8)));

    String timeStr = "";
    String rest = line;
    int idx = line.indexOf(" | ");
    if (idx > 0) { timeStr = line.substring(0, idx); rest = line.substring(idx + 3); }

    TextView timeTv = tv(timeStr, subC, 11);
    LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    tl.setMargins(px(8), 0, 0, 0);
    head.addView(timeTv, tl);

    TextView badgeTv = tv(badgeText, badgeColor, 10);
    badgeTv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    head.addView(badgeTv);
    box.addView(head);

    TextView contentTv = tv(rest, textC, 12);
    contentTv.setTypeface(Typeface.MONOSPACE);
    contentTv.setPadding(0, px(6), 0, 0);
    contentTv.setTextIsSelectable(true);
    box.addView(contentTv);
    return box;
}

void buildLog(LinearLayout box) {
    box.removeAllViews();
    try {
        ArrayList snap = new ArrayList();
        try { synchronized (callLog) { snap.addAll(callLog); } } catch (Exception e) {}

        if (snap.size() == 0) {
            LinearLayout emptyCard = card();
            TextView emptyTv = tv("暂无调用记录", subC, 13);
            emptyTv.setGravity(Gravity.CENTER);
            emptyTv.setPadding(0, px(28), 0, px(28));
            emptyCard.addView(emptyTv);
            box.addView(emptyCard);
            return;
        }
        for (int i = 0; i < snap.size(); i++) {
            box.addView(logCard((String) snap.get(i)));
        }
    } catch (Exception e) {
        box.addView(tv("记录加载失败: " + e.getMessage(), subC, 13));
    }
}

void restartServer() {
    serverOn[0] = false;
    try { if (serverSock[0] != null) serverSock[0].close(); } catch (Exception e) {}
    new Thread(new Runnable() {
        public void run() { startServer(); }
    }).start();
}

String buildMcpJson(String ip, int port, String token) {
    try {
        String url = "http://" + ip + ":" + port + "/mcp";
        JSONObject server = new JSONObject();
        server.put("url", url);
        JSONObject headers = new JSONObject();
        headers.put("Authorization", "Bearer " + token);
        server.put("headers", headers);
        JSONObject servers = new JSONObject();
        servers.put("qfun", server);
        JSONObject root = new JSONObject();
        root.put("mcpServers", servers);
        return root.toString(2);
    } catch (Exception e) {
        return "{\"error\":\"" + e.getMessage() + "\"}";
    }
}

void showPanel(Activity act) {
    try {
        applyTheme();
        checkBoxes.clear();
        toolNames.clear();
        catExpanded.clear();
        catCheckMap.clear();
        catItemsMap.clear();

        final int port = mcpNowPort();
        final boolean running = serverOn[0] && serverSock[0] != null && !serverSock[0].isClosed();

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(round(bgC, 20));
        root.setPadding(px(14), px(14), px(14), px(14));

        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.BOTTOM);
        titleRow.addView(tvBold("QFun MCP", textC, 22));
        TextView sub = tv("  控制台", subC, 13);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        slp.setMargins(px(4), 0, 0, px(3));
        titleRow.addView(sub, slp);
        root.addView(titleRow);

        LinearLayout statusRow = new LinearLayout(context);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        statusRow.setPadding(0, px(8), 0, 0);

        View statusDot = new View(context);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(running ? okC : errC);
        statusDot.setBackground(dotBg);
        statusRow.addView(statusDot, new LinearLayout.LayoutParams(px(10), px(10)));

        TextView statusTv = tv(running ? "运行中" : "未运行", running ? okC : errC, 13);
        statusTv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams stlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stlp.setMargins(px(6), 0, px(14), 0);
        statusRow.addView(statusTv, stlp);

        TextView portTv = tv("端口 " + port, textC, 13);
        LinearLayout.LayoutParams ptlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ptlp.setMargins(0, 0, px(14), 0);
        statusRow.addView(portTv, ptlp);

        statusCntTv = tv(enabledToolCount() + " / " + totalToolCount() + " 工具", textC, 13);
        statusRow.addView(statusCntTv);
        root.addView(statusRow);

        // 连接地址折叠
        final LinearLayout addrCard = new LinearLayout(context);
        addrCard.setOrientation(LinearLayout.VERTICAL);
        addrCard.setBackground(roundB(cardC, 14, borderC, 1));
        addrCard.setPadding(px(12), px(4), px(12), px(4));
        LinearLayout.LayoutParams acLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        acLp.setMargins(0, px(10), 0, 0);
        addrCard.setLayoutParams(acLp);

        final LinearLayout addrHead = new LinearLayout(context);
        addrHead.setOrientation(LinearLayout.HORIZONTAL);
        addrHead.setGravity(Gravity.CENTER_VERTICAL);
        addrHead.setPadding(px(2), px(8), px(2), px(8));
        addrHead.setBackground(rippleRound(cardC, 8));

        final ArrayList ips = getLocalIps();
        final TextView addrArrow = tv("▸", accentC, 14);
        addrArrow.setGravity(Gravity.CENTER);
        addrArrow.setLayoutParams(new LinearLayout.LayoutParams(px(18), px(18)));

        addrHead.addView(addrArrow);
        LinearLayout.LayoutParams atLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        atLp.setMargins(px(8), 0, px(6), 0);
        addrHead.addView(tvBold("连接地址", textC, 14), atLp);
        addrHead.addView(tv("(" + ips.size() + ")", subC, 12));
        addrCard.addView(addrHead);

        final LinearLayout addrBody = new LinearLayout(context);
        addrBody.setOrientation(LinearLayout.VERTICAL);
        addrBody.setPadding(0, px(2), 0, px(8));
        addrBody.setVisibility(View.GONE);

        final String ftok = getToken();

        for (int i = 0; i < ips.size(); i++) {
            final String ip = (String) ips.get(i);
            final String u = "http://" + ip + ":" + port + "/mcp";

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(px(4), px(6), px(4), px(6));

            TextView urlTv = tv(u, accentC, 12);
            urlTv.setTypeface(Typeface.MONOSPACE);
            urlTv.setTextIsSelectable(true);
            row.addView(urlTv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Button cpUrl = btnSmall("复制");
            cpUrl.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { copyText(u); }
            });
            LinearLayout.LayoutParams cpLp = new LinearLayout.LayoutParams(px(48), px(30));
            cpLp.setMargins(px(4), 0, px(4), 0);
            row.addView(cpUrl, cpLp);

            Button cpJson = btnSmall("JSON");
            cpJson.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { copyText(buildMcpJson(ip, port, ftok)); }
            });
            row.addView(cpJson, new LinearLayout.LayoutParams(px(52), px(30)));

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.setMargins(0, px(4), 0, 0);
            row.setLayoutParams(rowLp);
            addrBody.addView(row);
        }
        addrCard.addView(addrBody);

        addrHead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean now = addrBody.getVisibility() == View.VISIBLE;
                addrBody.setVisibility(now ? View.GONE : View.VISIBLE);
                addrArrow.setText(now ? "▸" : "▾");
            }
        });
        root.addView(addrCard);

        // Token 行
        String tokShow = ftok.length() > 16 ? ftok.substring(0, 8) + "..." + ftok.substring(ftok.length() - 4) : ftok;

        LinearLayout tokCard = new LinearLayout(context);
        tokCard.setOrientation(LinearLayout.HORIZONTAL);
        tokCard.setGravity(Gravity.CENTER_VERTICAL);
        tokCard.setBackground(roundB(cardC, 14, borderC, 1));
        tokCard.setPadding(px(12), px(8), px(8), px(8));
        LinearLayout.LayoutParams tclp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tclp.setMargins(0, px(8), 0, 0);
        tokCard.setLayoutParams(tclp);

        TextView tokLabel = tv("Token", subC, 12);
        LinearLayout.LayoutParams tllp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tllp.setMargins(0, 0, px(10), 0);
        tokCard.addView(tokLabel, tllp);

        TextView tokTv = tv(tokShow, textC, 12);
        tokTv.setTypeface(Typeface.MONOSPACE);
        tokCard.addView(tokTv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button tokCp = btnSmall("复制");
        tokCp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { copyText(ftok); }
        });
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(px(48), px(30));
        tcLp.setMargins(px(6), 0, px(4), 0);
        tokCard.addView(tokCp, tcLp);

        Button tokReset = btnSmall("重置");
        tokReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    putString("mcp", "token", "");
                    getToken();
                    toast("Token 已重置，重开面板查看");
                } catch (Exception e) {}
            }
        });
        tokCard.addView(tokReset, new LinearLayout.LayoutParams(px(48), px(30)));
        root.addView(tokCard);

        // 端口行
        LinearLayout portCard = new LinearLayout(context);
        portCard.setOrientation(LinearLayout.HORIZONTAL);
        portCard.setGravity(Gravity.CENTER_VERTICAL);
        portCard.setBackground(roundB(cardC, 14, borderC, 1));
        portCard.setPadding(px(12), px(8), px(8), px(8));
        LinearLayout.LayoutParams pclp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pclp.setMargins(0, px(8), 0, 0);
        portCard.setLayoutParams(pclp);

        TextView portLabel = tv("端口", subC, 12);
        LinearLayout.LayoutParams pllp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pllp.setMargins(0, 0, px(10), 0);
        portCard.addView(portLabel, pllp);

        final EditText portInput = new EditText(context);
        portInput.setHint("8765");
        portInput.setText(String.valueOf(port));
        portInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        portInput.setTextColor(textC);
        portInput.setHintTextColor(subC);
        portInput.setBackground(roundB(cardC2, 10, borderC, 1));
        portInput.setSingleLine(true);
        portInput.setPadding(px(10), 0, px(10), 0);
        portInput.setGravity(Gravity.CENTER_VERTICAL);
        portCard.addView(portInput, new LinearLayout.LayoutParams(0, px(36), 1f));

        Button saveBtn = btnPrimary("保存");
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int p = Integer.parseInt(portInput.getText().toString().trim());
                    if (p < 1024 || p > 65535) { toast("端口需在 1024-65535 之间"); return; }
                    putInt("mcp", "port", p);
                    restartServer();
                    toast("端口已保存，服务重启中");
                } catch (Exception e) { toast("端口无效"); }
            }
        });
        LinearLayout.LayoutParams slp2 = new LinearLayout.LayoutParams(px(64), px(36));
        slp2.setMargins(px(8), 0, 0, 0);
        portCard.addView(saveBtn, slp2);
        root.addView(portCard);

        // 全选/清空固定行
        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(px(2), px(8), px(2), 0);

        Button allBtn = btnSmall("全选");
        allBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { setAll(true); }
        });
        Button noneBtn = btnSmall("清空");
        noneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { setAll(false); }
        });

        cfgCountTv = tv("", subC, 12);
        cfgCountTv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        toolbar.addView(allBtn, new LinearLayout.LayoutParams(px(60), px(34)));
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(px(60), px(34));
        nLp.setMargins(px(8), 0, 0, 0);
        toolbar.addView(noneBtn, nLp);
        toolbar.addView(cfgCountTv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(toolbar);

        // Tab
        LinearLayout tabs = new LinearLayout(context);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackground(round(cardC2, 14));
        tabs.setPadding(px(4), px(4), px(4), px(4));
        LinearLayout.LayoutParams tabsLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tabsLp.setMargins(0, px(8), 0, 0);
        tabs.setLayoutParams(tabsLp);

        tabA = new Button(context);
        tabA.setAllCaps(false);
        tabA.setText("API 配置");
        tabA.setTextSize(14);
        tabA.setBackground(rippleRound(accentC, 12));
        tabA.setTextColor(Color.WHITE);
        try { tabA.setStateListAnimator(null); } catch (Exception e) {}
        tabA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { switchTab(false); }
        });
        tabs.addView(tabA, new LinearLayout.LayoutParams(0, px(40), 1f));

        tabB = new Button(context);
        tabB.setAllCaps(false);
        tabB.setText("调用记录");
        tabB.setTextSize(14);
        tabB.setBackground(rippleRoundB(cardC2, 12, 0, 0));
        tabB.setTextColor(subC);
        try { tabB.setStateListAnimator(null); } catch (Exception e) {}
        tabB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { switchTab(true); }
        });
        tabs.addView(tabB, new LinearLayout.LayoutParams(0, px(40), 1f));
        root.addView(tabs);

        // 内容区
        cfgBox = new LinearLayout(context);
        cfgBox.setOrientation(LinearLayout.VERTICAL);
        logBox = new LinearLayout(context);
        logBox.setOrientation(LinearLayout.VERTICAL);

        cfgScroll = new ScrollView(context);
        cfgScroll.setVerticalScrollBarEnabled(false);
        cfgScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        cfgScroll.addView(cfgBox);

        logScroll = new ScrollView(context);
        logScroll.setVerticalScrollBarEnabled(false);
        logScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        logScroll.addView(logBox);

        FrameLayout contentFrame = new FrameLayout(context);
        contentFrame.addView(cfgScroll, new FrameLayout.LayoutParams(-1, -1));
        contentFrame.addView(logScroll, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        contentLp.setMargins(0, px(10), 0, 0);
        root.addView(contentFrame, contentLp);

        buildCfg(cfgBox);
        buildLog(logBox);
        switchTab(false);

        panelDlg = new Dialog(act, android.R.style.Theme_Translucent_NoTitleBar);
        panelDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        panelDlg.setContentView(root);
        panelDlg.setCanceledOnTouchOutside(true);

        Window w = panelDlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.dimAmount = 0.55f;
            lp.width = Math.min(px(400), act.getResources().getDisplayMetrics().widthPixels - px(28));
            lp.height = (int)(act.getResources().getDisplayMetrics().heightPixels * 0.82);
            w.setAttributes(lp);
        }
        panelDlg.show();
    } catch (Exception e) {
        toast("面板打开失败: " + e.getMessage());
    }
}

void launchPanel() {
    Activity act = getNowActivity();
    if (act == null) {
        toast("请先打开 QQ 界面再打开面板");
        return;
    }
    act.runOnUiThread(new Runnable() {
        public void run() {
            showPanel(getNowActivity());
        }
    });
}