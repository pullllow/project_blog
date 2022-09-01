layui.use('layim', function (layim) {

    // //客服模式
    // layim.config({
    //     brief: true, //是否简洁模式 true 不显示主面板
    //     min: true
    // }).chat({
    //     name: '客服姐姐',
    //     type: 'friend',
    //     avatar: 'http://tva1.sinaimg.cn/default/images/default_avatar_male_180.gif',
    //     id: -2
    // });
    // layim.setChatMin(); // 收缩聊天面板


    layim.config({
        brief: true //是否简洁模式 true 不显示主面板
        ,voice: false
        ,chatLog: layui.cache.dir + 'css/modules/layim/html/chatlog.html'
    });

    var $ = layui.jquery;

    var tioWs = new tio.ws($, layim);


    //获取个人、群聊信息并打开聊天窗口
    tioWs.openChatWindow();

    // 建立连接 websocket
    tioWs.connect();


    //历史聊天信息回显
    tioWs.initHistoryMsg();

    // 接受消息


    // 发送消息
    // 发送消息时 layim 框架会自动显示发送的消息，同时t-io会将发送的消息呈现
    layim.on('sendMessage', function (res) {
        tioWs.sendChatMsg(res);
    });


    // 心跳、断开重连机制



});