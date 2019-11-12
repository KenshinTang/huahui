var WXAJAX = new function() {
    var _xhr, t = this;

    function finish(v, cb) {
        if (cb == null) return;
        var o;
        if (v && v.length > 1) {
            try { o = JSON.parse(v); } catch (e) { o = v; }
        }
        cb && cb(o);
    }

    function deobj(obj) {
        if (obj == null) return '';
        var s = [];
        for (var i in obj) {
            if (typeof(obj[i]) == typeof('')) {
                if (obj[i].indexOf('%') > 0)
                    obj[i] = obj[i].replace(/%/g, "%25");
                if (obj[i].indexOf('&') > 0)
                    obj[i] = obj[i].replace(/\&/g, "%26");
                if (obj[i].indexOf('+') > 0)
                    obj[i] = obj[i].replace(/\+/g, "%2B");
            }
            s.push(i + '=' + obj[i]);
        }
        return s.join('&');
    }

    function init(post, url, data, cb) {
        !data && (data = {});
        url = t.Uri() + url;
        if (!post) {
            url += (url.indexOf("?") <= -1 ? "?" : "&") + "_t=" + Math.random();
        } else {
            data._t = Math.random();
        }
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (this.readyState == 4) {
                if (this.status == 200) {
                    finish(this.responseText, cb);
                } else {
                    cb && cb({ code: 0, msg: "服务器异常" });
                }
            }
        };
        xhr.open(post ? 'POST' : 'GET', url, true);
        xhr.setRequestHeader("Accept", "application/json");
        if (post) {
            data = deobj(data);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        }
        xhr.send(data);
    }
    /*----AJAX公用方法-----*/

    /*获取服务器接口根路径*/
    t.Uri = function() {
        return "http://app.huahuiyun8.com/";
        //return "http://test.huahuiyun8.com/";
    };
    /*执行GET方法，一般用于从服务器获取数据，api长度尽量不超过1000字节*/
    t.GET = function(api, cb) {
        init(false, api, null, cb);
    };
    /*执行POST方法，一般用于向服务器提交数据，data建议不为空*/
    t.POST = function(api, data, cb) {
        init(true, api, data, cb);
    };
};
if (self !== top)
    window.wx = top.wx;

var WXMethod = {
    url: 'http://app.huahuiyun8.com/',
    //url: "http://test.huahuiyun8.com/",
    getTK: function(c, cb) {
        WXAJAX.POST("api/pay/getwechatToken", { code: c }, function(r) {
            var d = null;
            if (r.data)
                try { d = JSON.parse(r.data) } catch (e) {}
            if (r.code === 1 && d && !d.errcode) {
                cb && cb(d)
            } else {
                WXMethod.err("微信验证失败，请稍后再试");
            }
        });
    },
    getInfo: function(openid, access_tk, cb) {
        WXAJAX.GET("api/pay/uinfo?openid=" + openid + "&accesstoken=" + access_tk, function(r1) {
            var d1 = null;
            if (r1.data)
                try { d1 = JSON.parse(r1.data) } catch (e) {}
                // WXMethod.notice(JSON.stringify(r1))
            if (r1.code === 1 && d1 && !d1.errcode) cb && cb(d1);
            else {
                Comm.db('access_token', '');
                Comm.db('openid', '');
                location.reload();
                WXMethod.err("微信获取用户信息失败，请稍后再试");
            }
        });
    },
    wxLogin: function(openid, unionid, nickname, headimgurl, cb) {
        var data = { openid: openid, wxunionid: unionid, customerName: nickname, avatar: headimgurl, clientType: Comm.db('_shopid') ? 64 : 2, loginType: 1 }
        Comm.db("wxData", data);

        WXAJAX.POST("/common/customer/otherLogin", data, function(r2) {
            cb && cb(r2);
        })
    },
    sign: function(cb) {
        WXAJAX.POST("/api/pay/getWechatSignature", { locationUrl: top.location.href.split("#")[0] }, function(r3) {
            if (r3.code === 1 && r3.data) {
                cb && cb(r3.data);
            } else {
                WXMethod.err("微信页面授权失败，请稍后再试")
            };
        })
    },
    err: function(msg) {
        Comm.message(msg)
    },
    notice: function(msg) {
        var box = document.getElementById("box");
        if (box) {
            box.style.display = "block";
            box.value = msg;
        }
    },

    exready: 0,
    config: function(auths, success) {
        WXMethod.sign(function(d) {
            wx.config({
                debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
                appId: d.appId, // 必填，公众号的唯一标识
                timestamp: d.timestamp, // 必填，生成签名的时间戳
                nonceStr: d.nonceStr, // 必填，生成签名的随机串
                signature: d.signature, // 必填，签名，见附录1
                jsApiList: auths // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
            });

            wx.ready(function() {
                // config信息验证后会执行ready方法，所有接口调用都必须在config接口获得结果之后，config是一个客户端的异步操作，所以如果需要在页面加载时就调用相关接口，则须把相关接口放在ready函数中调用来确保正确执行。对于用户触发时才调用的接口，则可以直接调用，不需要放在ready函数中。
                WXMethod.exready = 1;
                success && success();
            });

            wx.error(function(res) {
                // config信息验证失败会执行error函数，如签名过期导致验证失败，具体错误信息可以打开config的debug模式查看，也可以在返回的res参数中查看，对于SPA可以在这里更新签名。
                WXMethod.exready = -1;
            });
        })
    },
    pay: function(d, cb) {
        wx.chooseWXPay({
            appId: d.appId,
            timestamp: d.timeStamp,
            nonceStr: d.nonceStr,
            package: d.package,
            signType: d.signType,
            paySign: d.paySign,
            success: function(res) {
                cb && cb({ code: 1 });
            },
            fail: function(res) {
                Comm.message(res);
                cb && cb({ code: 0, msg: '微信支付失败' });
            },
            cancel: function(res) {
                cb && cb({ code: 0, msg: '用户取消支付' });
            }
        });
    }
};

var Share = {
    start: function() {
        var e = top.document.createElement("DIV");
        e.className = "_shareWin";
        top.document.body.appendChild(e);
        e.onclick = function() {
            e.parentNode.removeChild(e);
        }
    },
    config: function(o, success, error) {
        var c = {
            title: o.title, // 分享标题
            desc: o.desc, // 分享描述
            link: o.link, // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: o.imgUrl, // 分享图标
            success: function() { success && success(); },
            cancel: function() { error && error(); }
        };
        wx.onMenuShareAppMessage(c);
        wx.onMenuShareQQ(c);
        wx.onMenuShareWeibo(c);
        wx.onMenuShareQZone(c);
    }
};

//普通网页不进行微信授权
if (navigator.userAgent.toLocaleLowerCase().indexOf('micromessenger') < 0) {
    for (var i in WXMethod) {
        if (typeof WXMethod[i] === "function") WXMethod[i] = function() {};
    }
}