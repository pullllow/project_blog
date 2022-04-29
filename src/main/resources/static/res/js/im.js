if (typeof (tio) == "undefined") {
    tio = {};
}

tio.ws = {};
tio.ws = function ($, layim) {

    var self = this;

    this.connect = function () {

        var url = "ws://localhost:9326?userId=" + self.userId;
        var socket = new WebSocket(url);

        self.socket = socket;

        socket.onopen = function () {
            console.log("t-io websocket 启动~");
        };

        socket.onclose = function () {
            console.log("t-io websocket 关闭~");
        };

        socket.onmessage = function (res) {
            console.log("接受到消息");
            console.log(res);



            var msgBody = eval('(' + res.data + ')');
            if(msgBody.emit === 'chatMessage') {
                layim.getMessage(msgBody.data);
            }



        };


    }

    this.openChatWindow = function () {

        // 获取个人信息
        $.ajax({
            url: "/chat/getMineAndGroupData",
            async: false,
            success: function (res) {
                self.group = res.data.group;
                self.mine = res.data.mine;
                self.userId = self.mine.id;

            }
        });

        console.log(self.group);
        var cache = layui.layim.cache();
        cache.mine = self.mine;

        // 打开窗口
        layim.chat(self.group);
        layim.setChatMin(); //收缩聊天面板

    }

    this.sendChatMsg = function (res) {
        self.socket.send(JSON.stringify({
            type: 'chatMessage', //用于在服务端区分消息类型
            data: res
        }));

    }

    this.initHistoryMsg = function () {

    }
}