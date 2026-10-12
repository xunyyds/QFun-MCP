boolean[] serverOn = new boolean[1];
ServerSocket[] serverSock = new ServerSocket[1];
String[] serverAddr = new String[1];
ArrayList callLog = new ArrayList();
boolean[] runScriptBusy = new boolean[1];
int runScriptMaxTimeoutSeconds = 30;
int[] activeMcpConnections = new int[1];

String rootPath = pluginPath + "/API/";

loadJava(rootPath + "import.java");
loadJava(rootPath + "mcp_server.java");
loadJava(rootPath + "mcp_tools.java");
loadJava(rootPath + "mcp_ui.java");

addItem("MCP 面板", "openPanel");

new Thread(new Runnable() {
    public void run() {
        startServer();
    }
}).start();

void openPanel(int chatType, String peerUin, String name) {
    launchPanel();
}

void unLoadPlugin() {
    try {
        serverOn[0] = false;
        if (serverSock[0] != null) serverSock[0].close();
    } catch (Exception e) {}
}