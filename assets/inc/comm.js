var config = {
    root: 'http://app.huahuiyun8.com/',
    // root: 'http://test.huahuiyun8.com/',
    // root: 'http://192.168.0.23:7700/',
    // root: 'http://192.168.0.129:80/', //牟军
    webroot: "http://app.huahuiyun8.com/app", //网址地址
    //webroot: "http://test.huahuiyun8.com/app_test", //网址地址
    // base: '/chuanhui/',//本地
    // baseimg: '/',//本地
    base: '', //线上
    baseimg: '../', //线上
    isTest: false,
    ossroot: "http://huahuiapp.oss-cn-shenzhen.aliyuncs.com/",
    pagesize: 10,
}


/*app 公用方法处理器*/
var app = new function() {
    /*密码框切换显示和关闭*/
    /*o 参数是点击图片*/
    /*调用 app.checkimg(this)*/
    this.checkimg = function(o) {
        if (o.src.indexOf("open") >= 0) {
            o.src = "../img/login/close.png"
            $(o).parent().parent().find("input[type='text']").attr("type", "password")
        } else {
            o.src = "../img/login/open.png"
            $(o).parent().parent().find("input[type='password']").attr("type", "text")
        }
    }

    /*发送验证码公用方法*/
    /*o 点击发送验证码按钮*/
    /*phone 发送短信手机号*/
    /*type 验证码类型*/
    /*imgcode 图片验证码，可不传*/
    /*调用 app.timeCountDown(this,15928877394,1,5421)*/
    //按钮倒计时
    var wait = 60,
        timeCountDownclick = true;
    this.timeCountDown = function(o, phone, type, imgcode) {
        imgcode = imgcode == undefined ? '' : imgcode;
        if (!phone) {
            Comm.message("请输入手机号");
            return;
        }
        var reg = /^1\d{10}$/;
        if (phone && !reg.test(phone)) {
            Comm.message("手机格式错误");
            return;
        }
        if (timeCountDownclick) {
            timeCountDownclick = false;
            o.setAttribute("disabled", true);

            AJAX.GET('/common/sms/send?phone=' + phone + '&type=' + type + "&imgcode=" + imgcode, function(d) {
                if (d.code == 1) {
                    Comm.message('短信已发送，请注意查收');
                    var i = setInterval(function() {
                        if (wait == 0) {
                            o.removeAttribute("disabled");
                            o.innerText = "重新发送";
                            wait = 60;
                            clearInterval(i);
                        } else {
                            o.innerText = wait + '秒后重发';
                            wait--;

                        }
                    }, 1000);
                } else {
                    o.removeAttribute("disabled");
                    Comm.message(d.msg);
                }
                timeCountDownclick = true;
            });
        }
    }

    /* 去掉字符串首位空格 */
    this.trim = function(s) {
        return s.replace(/(^\s*)|(\s*$)/g, "");
    }

    //验证是否登录
    this.isLogin = function() {
            var user = Comm.db('userinfo');
            var token = Comm.db('_token');
            if ((!user || !token)) {
                return false; //没有登录
            }
            return true; //登录
        }
        //验证是否登录，并且提示登录
    this.checkIslogin = function(show, url) {
        var user = Comm.db('userinfo');
        if ((!user)) {
            if (show == true) {
                Comm.confirm('您还未登录是否跳转到登录界面？', function(a) {
                    if (a == 1) {
                        Comm.gotop('login.html')
                    }
                });
            }
            return false;
        }
        if (url) {
            Comm.go(url)
        }
        return true;
    }

    //处理时间函数
    this.formatDate = function(timestamp, formats) {
        // formats格式包括
        // 1. Y-m-d
        // 2. Y-m-d H:i:s
        // 3. Y年m月d日
        // 4. Y年m月d日 H时i分
        formats = formats || 'Y-m-d';

        var zero = function(value) {
            if (value < 10) {
                return '0' + value;
            }
            return value;
        };

        if (typeof(timestamp) == typeof("")) {
            timestamp = timestamp.replace(/-/g, "/").replace(/\.0/g, "")
        }
        var myDate = timestamp ? new Date(timestamp) : new Date();

        var year = myDate.getFullYear();
        var month = zero(myDate.getMonth() + 1);
        var day = zero(myDate.getDate());

        var hour = zero(myDate.getHours());
        var minite = zero(myDate.getMinutes());
        var second = zero(myDate.getSeconds());

        return formats.replace(/Y|m|d|H|i|s/ig, function(matches) {
            return ({
                Y: year,
                m: month,
                d: day,
                H: hour,
                i: minite,
                s: second
            })[matches];
        });
    }

    //处理头像加载失败
    this.herrorimg = function(a) {
            a.src = config.baseimg + 'img/wjg.png'
            a.onerror = null;
        }
        //处理图片加载失败
    this.errorimg = function(a) {
            a.src = config.baseimg + 'img/error.png'
            a.onerror = null;
        }
        //验证空字符串
    this.nullReplace = function(str) {
        if (!str || isEmpty(str)) return '';
        return str;
    }

    //合并数组
    this.assign = function(obj1, obj2) {

            var tmp = {};
            for (var i in arguments) {
                for (var key in arguments[i]) {
                    tmp[key] = arguments[i][key];
                }
            }
            return tmp;;
        }
        //处理数字工具
        /*
            v:转换的数字
            d:保留的位数
        */
    this.conunm = function(v, d) {
        if (v == undefined) {
            return 0;
        }
        if (v == 0) {
            return v;
        }
        if (d == undefined) {
            d = 3;
        }
        if (v > 10000) {
            return (Number(v) / 10000).toFixed(d) + "万"
        }
        if (v != null && v != "" && v != undefined) {
            if (v.toString().indexOf(".") >= 0) {
                //四舍五入
                return Number(v).toFixed(d);
                //向下取整
                //Math.floor(v * 100) / 100;
            } else {
                return v;
            }
        }
    }


    this.price = function(v) {
        if (v == 0) {
            return v;
        }
        if (v != null && v != "" && v != undefined) {
            if (v.toString().indexOf('.') > 0) {
                var j = v;
                if (j.toString().indexOf('.') > 0) {
                    j = j.toString().split('.');
                    return j[0] + "." + j[1].substring(0, 2);
                }
                return j;
            } else {
                return v;
            }
        }
        return 0;
    }

    //获取配置系统信息；
    this.getSys = function(keys, cb) {
            AJAX.GET('/api/config/bykeys?keys=' + keys, function(d) {
                if (d.code == 1 && d.data.length > 0) {
                    cb && cb(d.data);
                }
            });
        }
        //clientType 1:供应商 3：采购端 4 分拣中心
    this.update = function(clientType) {
        //软件升级
        if (Comm.w9()) {
            Comm.getVersion(function(d) {
                var clientSys = Comm.ios() ? 2 : 1; //1 android 2 ios
                AJAX.GET('/api/update/getupd?clientType=' + clientType + "&clientSys=" + clientSys, function(a) {
                    if (a.code == 1) {
                        if (d.versionName != a.data.number) {
                            Comm.alert('已有版本更新，请前往下载', function() {
                                Comm.openUrlStr(a.data.url)
                            })
                        }
                    }
                })
            })
        }
    }

    this.Add = function(num1, num2) {
        return app.price((num1 * 1000 + num2 * 1000) / 1000) * 1;
    }
    this.Subtract = function(num1, num2) {
        return app.price((num1 * 1000 - num2 * 1000) / 1000) * 1;
    }
    this.Multiply = function(num1, num2) {
        return app.price(num1 * num2) * 1;
    }
    this.Except = function(num1, num2) {
        return app.price(num1 / num2) * 1;
    }
}

//输入框输入格式限制
//tag传入值：'number'(只能输入和粘贴数字)
function inputLimit(tag) {
    var arr = document.querySelectorAll('input[' + tag + ']');
    for (var i = 0; i < arr.length; i++) {
        var inputE = arr[i]
        inputE.onchange = chnu;
        inputE.onafterpaste = chnu;
    }

    function chnu(e) {
        e = (event || e).target;
        e.value = e.value.replace(/\D/g, '');
    }
}

//数组删除扩展
Array.prototype.del = function(n) {
    //n表示第几项，从0开始算起。
    //prototype为对象原型，注意这里为对象增加自定义方法的方法。
    if (n < 0) //如果n<0，则不进行任何操作。
        return this;
    else
        return this.slice(0, n).concat(this.slice(n + 1, this.length));
}

/*
*公用验证类
*如需扩展对应验证， 在reg对象内添加对应验证规则即可
* 111
*  1加input属性 data-reg='m',  m == 对应reg正则；
*  2加input属性 name =‘phone’ 或 data-type='phone',  'phone' == 设置成对应ajax参数属性 （注：未设置input的name属性此项直接跳过）
*  3选填 data-nocheck='true'; 如果有 此属性 则跳过此项验证
*调用方式1）单个验证触发 需结合事件绑定对应标签 onblur="Check.check(this)"
 调用方式2）批量验证 Check.submit('GET/POST')" 验证全部通过返回参数值，验证有误返回false；
*/
var Check = (function(g) {
    var inputArr = {}; //保存返回内容
    var falg = false; //初始状态
    var reg = {
            empty: {
                t: '请输入必填项',
                e: /^[/[\S]+/
            },
            m: {
                t: '手机号码格式错误',
                e: /^1\d{10}$/
            },
            m2: {
                t: '推荐码格式错误（上级手机号）',
                e: /^1\d{10}$/
            },
            p: {
                t: '密码格式错误（6-16位）字母或者数字',
                e: /^[a-zA-Z0-9]{6,16}$/
            },
            c: {
                t: '验证码格式错误',
                e: /^\d{4}$/
            },
            length4: {
                t: '位数错误（4-20位）',
                e: /^.{4,20}$/
            },
            required: {
                t: '必填项',
                e: /^.{1,999}$/
            },
            money: {
                t: '价格格式错误',
                e: /(^[1-9]\d*(\.\d{1,2})?$)|(^0(\.\d{1,8})?$)/
            },
            amount: {
                t: '数量格式错误',
                e: /(^[1-9]\d*(\.\d{1,2})?$)|(^0(\.\d{1,8})?$)/
            },
            bankac: {
                t: '位数错误（10-20位）',
                e: /^.{10,20}$/
            },
            email: {
                t: '邮箱格式错误',
                e: /^[A-Za-z0-9\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
            },
            nz: {
                t: '请输入正整数',
                e: /^\d+$/
            },
            nzfloat: {
                t: '请输入正确数字',
                e: /(^[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\.[0-9]([0-9])?$)/
            },
            rn: {
                t: '请输入正确姓名',
                e: /[\u4E00-\u9FA5]{2,5}(?:·[\u4E00-\u9FA5]{2,5})*/
            },
            cd: {
                t: '请输入正确身份证号',
                e: /(^\d{15}$)|(^\d{17}(x|X|\d)$)/
            },
            bcd: {
                t: '请输入正确银行卡号',
                e: /^([1-9]{1})(\d{14,18})$/
            }
        }
        //g.reg = reg // 挂在Comm对象上去
    return new function() {
        //验证方法 【需使用事件绑定绑定对应html标签】
        //批量验证调用this.submit('POST')方法 返回 验证通过的对应参数；
        this.check = function(t, e) {
                var _t = t.dataset['reg']; //需验证
                var nocheck = t.dataset['nocheck']; // nocheck ==‘true’跳过验证
                if (typeof nocheck == "string" && nocheck == "true") {
                    return true;
                } else {
                    if (_t && _t != "") {
                        var regs = _t.split(' ');
                        for (var i = 0; i < regs.length; i++) {
                            if (regs[i] != "") {
                                if (!regfun(regs[i])) {
                                    return false;
                                }
                            }
                        }
                    } else {
                        //验证通过
                        t.dataset['iserror'] = 1;
                        return true;
                    }

                    function regfun(v) {
                        if (!reg[v]['e'].test(t.value)) {
                            //验证错误
                            if (e) {
                                var placeholder = t.getAttribute('placeholder');
                                if (placeholder != undefined && placeholder != "" && placeholder != null && v == "empty") {
                                    Comm.message(placeholder);
                                } else {
                                    Comm.message(reg[v]['t']);
                                }
                            }
                            t.dataset['iserror'] = 0;
                            return false;
                        } else {
                            //验证通过
                            t.dataset['iserror'] = 1;
                            return true;
                        }
                    }

                }
            }
            //验证状态 ,返回元素数组
        this.st = function() {
                var _this = this;
                inputArr['okay'] = [];
                inputArr['iserror'] = [];
                //对应的每个值
                $.each($('input[name],textarea[name]'), function(i, e) {
                    var v = e.value;
                    var k = e.name || $(e).data('type');
                    //返回保存的数据
                    //var arr = [];
                    //验证每个带name属性的input
                    _this.check(e);
                    var kv = new Array;
                    if (k) {
                        kv.push(k, v);
                    };
                    //失败的
                    if (e.dataset['iserror'] == "0" && (e.dataset['nocheck'] == "false" || e.dataset['nocheck'] == undefined)) {
                        inputArr['iserror'].push(e); //验证失败的DOM
                    } else {
                        inputArr['okay'].push(kv); //验证通过的
                    }
                })
                return inputArr;
            }
            //返回初始状态
        this.stDefault = function() {
            return falg;
        };
        this.stDefault.msg = '请填写完整信息';

        //提交数据前步骤 { 验证 + 格式化参数 }
        this.submit = function(action) {
            //falg = true; //重置初始状态
            /*if (!this.stDefault()) {
                Comm.message(this.stDefault.msg);
                return false;
            }*/

            var arr = this.st();
            var errorObj = arr['iserror']; //错误的HtmlDom
            var dataArr = arr['okay']; //返回保存的数据
            var opt = null; //存ajax参数
            //格式化参数
            function optFormat(_opt) {
                $.each(dataArr, function(i, e) {
                    if (typeof _opt == 'object') {
                        _opt[e[0]] = e[1];
                    } else {
                        _opt += ('&' + e[0] + '=' + e[1]);
                    }
                });
                return _opt;
            }

            if (arr['iserror'].length > 0) { //检查是否有验证错误未通过
                this.check(errorObj[0], true);
                return false;
            } else if (action == 'POST' || action == 'post') {
                //post格式参数
                opt = optFormat(opt = {});
            } else {
                // 默认 get格式参数
                opt = optFormat(opt = '');
            }
            return opt;
        }
    };
}(window['Comm']));

//底部v  Foot.init 渲染
var Foot = new function() {
    var list = [{
            cls: 'index',
            name: '首页'
        },
        {
            cls: 'sl_askForLeave',
            name: '请假'
        },
        {
            cls: 'cart',
            name: '购物车'
        },
        {
            cls: 'my',
            name: '我的'
        }
    ];

    var user = Comm.db('userinfo');

    if (user && user.userType == 5) {
        var item = {
            cls: 'classManage',
            name: '班级管理'
        };
        list.splice(2, 0, item);
    }

    this.init = function() {
        Comm.setAndroidHome();
        var oUL = document.createElement('ul');
        var frag = document.createDocumentFragment();
        oUL.className = 'footer-con';

        //判断链接是否存在
        function check(t) {
            return location.href.indexOf(t) >= 0;

        };
        var isclose = 0;
        if (location.href.indexOf('index') > 0) {
            isclose = 1;
        }
        for (var i = 0, l = list.length; i < l; i++) {
            var _ = list[i];
            var oLi = document.createElement('li');
            oLi.setAttribute('class', 'footer-item ' + _.cls + " " + (check(_.cls + ".htm") ? 'colorred' : ''));
            oLi.setAttribute('onclick', 'Comm.goself(\'' + _.cls + '.html\')');
            oLi.style.backgroundImage = "url(img/" + ((check(_.cls + ".htm") ? _.cls + "-r" : _.cls)) + ".png)"
            oLi.innerHTML = _.name;
            frag.appendChild(oLi);
        }
        oUL.appendChild(frag);
        $('.shared').html(oUL);
        Comm.resizeSection();
    }
};

//上拉
//下拉
//请求地址  / 请求类型
//数据返回
//空数据图片 、空数据提示文字、空数据按钮文字 、 空数据按钮事件
//一个主动调用的下拉刷新的事件
function MERefresh(selector, config) {

    var t = this;

    var refreshOption = {
        mescrollId: '', //刷新容器的id----------------------必须
        clearEmptyId: '', //list列表的id----------------------必须

        //下拉刷新配置
        needHeadRefresh: true, //是否支持下拉刷新
        refreshUrl: null, //刷新的接口----------------------必须
        refreshParm: {}, //刷新的参数
        refreshTypeGet: true, //下拉是否为get请求----------------------必须
        refresh_cb: null,

        //上拉加载配置
        needFootRefresh: true, //是否支持上拉加载
        moreUrl: null, //加载更多的接口 -- 如果没有默认为刷新的接口
        moreParm: {}, //上拉加载的参数
        moreTypeGet: true, //上拉是否为get请求
        more_cb: null,
        pagesize: 10,

        //没有数据时的配置
        nodataTip: '抱歉，没有找到相关信息~',
        nodataButtonTex: '点击刷新',
        nodataClicked: function(d) { //点击按钮的回调,默认null
            t.MeRefScroll.triggerDownScroll();
        },
        hasHeadRefCb: false,
        hasfootRefCb: false,
    };
    //合并参数 json
    refreshOption = app.assign(refreshOption, config);

    //ajax
    function SLNetwork(rqType, apistr, params, cb, addMore) {
        var newParms = params;
        var PgInfo = {
            pagesize: 10,
            pageno: 1
        };
        if (params.pagesize > 0) {
            newParms.pagesize = params.pagesize;
        } else {
            newParms.pagesize = PgInfo.pagesize;
        }
        if (params.pageno > 0) {
            newParms.pageno = params.pageno;
        } else {
            newParms.pageno = PgInfo.pageno;
        }
        skipNullValueKey(newParms);

        //将字典中的空值或空字符串的 key-value删除
        function skipNullValueKey(params) {
            // for (var key in params) {
            //     if (params[key] == '' || params[key] == null) {
            //         delete params[key];
            //     }
            // }
            return params;
        };

        if (rqType == 'post') {
            AJAX.POST(apistr, newParms, function(data) {
                if (cb) {
                    cb(data)
                }
                if (data.code == 1 && addMore) {
                    if (newParms.pagesize * newParms.pageno >= data.totalCount) {
                        newParms.pageno -= 1;
                    }
                } else {
                    newParms.pageno = data.curPage;
                }
            });
        } else if (rqType == 'get') {
            AJAX.GET(appendApi(apistr, newParms), function(data) {
                if (cb) {
                    cb(data)
                }
                if (data.code == 1 && addMore) {
                    if (newParms.pagesize * newParms.pageno >= data.totalCount) {
                        newParms.pageno -= 1;
                    }
                } else {
                    newParms.pageno = data.curPage;
                }
            });
        }
    }

    //initScroll
    function initMeScroll(mescrollId, clearEmptyId) {
        var opt = t.refreshOption;
        opt.hasHeadRefCb = opt.refresh_cb ? true : false;
        opt.hasHeadRef = opt.more_cb ? true : false;

        opt.clearEmptyId = clearEmptyId || opt.clearEmptyId;
        opt.mescrollId = mescrollId || opt.mescrollId;

        opt.nodataTip = opt.nodataTip ? opt.nodataTip : '抱歉，没有找到相关信息~';
        opt.nodataButtonTex = opt.nodataButtonTex ? opt.nodataButtonTex : '点击刷新';
        opt.btnClick = opt.btnClick || function() { //点击按钮的回调,默认null
            mescroll.triggerDownScroll()
        };

        var optionParm = {
            //下拉刷新的所有配置项
            down: {
                use: opt.needHeadRefresh, //是否初始化下拉刷新; 默认true
                callback: refreshFunction,
                offset: 60, //触发刷新的距离,默认80
                auto: false,
                autoShowLoading: true,

                outOffsetRate: 0.2, //超过指定距离范围外时,改变下拉区域高度比例;值小于1且越接近0,越往下拉高度变化越小;
                bottomOffset: 20, //当手指touchmove位置在距离body底部20px范围内的时候结束上拉刷新,避免Webview嵌套导致touchend事件不执行
                minAngle: 45, //向下滑动最少偏移的角度,取值区间  [0,90];默认45度,即向下滑动的角度大于45度则触发下拉;而小于45度,将不触发下拉,避免与左右滑动的轮播等组件冲突;
                hardwareClass: "mescroll-hardware", //硬件加速样式;解决iOS下拉因隐藏进度条而闪屏的问题,参见mescroll.css
            },
            //上拉加载的所有配置项
            up: {
                // isLock: true,
                use: opt.needFootRefresh, //是否初始化上拉加载; 默认true
                callback: moreFunction, //上拉回调,此处可简写; 相当于 callback: function (page, mescroll) { getListData(page); }
                page: {
                    num: 0, //当前页 默认0,回调之前会加1; 即callback(page)会从1开始
                    size: opt.pagesize, //每页数据条数
                    time: null //加载第一页数据服务器返回的时间; 防止用户翻页时,后台新增了数据从而导致下一页数据重复;
                },
                empty: {
                    icon: "../img/wjg.png", //图标,默认null
                    tip: opt.nodataTip, //提示
                    btntext: opt.nodataButtonTex, //按钮,默认""
                    btnClick: opt.btnClick,
                },
                auto: true, //是否在初始化时以上拉加载的方式自动加载第一页数据; 默认true
                clearId: opt.clearEmptyId, //加载第一页时需清空数据的列表id; 如果此项有值,将不使用clearEmptyId的值;
                clearEmptyId: opt.clearEmptyId, //相当于同时设置了clearId和empty.warpId; 简化写法,默认null;
                noMoreSize: 1, //如果列表已无数据,可设置列表的总数量要大于半页才显示无更多数据;避免列表数据过少(比如只有一条数据),显示无更多数据会不好看
                offset: 60, //离底部的距离
                isBounce: false, //是否允许ios的bounce回弹;默认true,允许回弹; 此处配置为false,可解决微信,QQ,Safari等等iOS浏览器列表顶部下拉和底部上拉露出浏览器灰色背景和卡顿2秒的问题
            }
        }
        if (!t.refreshOption.refresh_cb) {
            optionParm.down = null;
        }

        var mescroll = new MeScroll(opt.mescrollId, optionParm);
        t.MeRefScroll = mescroll;
        return mescroll;
    }

    t.refreshOption = refreshOption;
    var _selector = selector ? selector.split(',') : ''; //Document element元素
    t.configurationDone = (typeof _selector == 'object') ? initMeScroll(_selector[0], _selector[1]) : function initRefresh(mescrollId, clearEmptyId) {
            initMeScroll(mescrollId, clearEmptyId);
        }
        //刷新调用
    t.refreshFunction = refreshFunction;

    function refreshFunction(mscroll) {
        console.log('——————————————————————————————————————————————————————刷新');
        // 
        if (!t.refreshOption.refresh_cb && !t.refreshOption.more_cb) {
            setTimeout(function() {
                t.MeRefScroll.endErr();
            }, 300)
            return;
        }
        console.log('准备调用刷新的请求');
        var pageParams = {};
        pageParams.pageno = 1,
            pageParams.curpage = 1;
        pageParams.pagesize = t.refreshOption.pagesize;

        var netType = t.refreshOption.refreshTypeGet ? 'get' : 'post';
        var netApi = t.refreshOption.refreshUrl;
        var netParms = t.refreshOption.refreshParm;

        var newNetParms = app.assign(netParms, pageParams); //合并两个对象

        SLNetwork(netType, netApi, newNetParms, function(data) {
            console.log('完成刷新的请求');
            endRefresh();
            if (!data.data) {
                data.data = [];
            }
            if (data.code == 1) {
                t.refreshOption.refresh_cb && t.refreshOption.refresh_cb(t, data);
                // t.MeRefScroll.resetUpScroll();
            } else {
                t.refreshOption.refresh_cb(t, data);
                //联网失败的回调,隐藏下拉刷新和上拉加载的状态;
                t.MeRefScroll.endErr();
            }
        });

    }

    //加载更多调用
    function moreFunction(page, d, v) {
        console.log('——————————————————————————————————————————————————————加载更多');
        //		
        if (!t.refreshOption.refresh_cb && !t.refreshOption.more_cb) {
            setTimeout(function() {
                t.MeRefScroll.endErr();
            }, 300)
            return;
        }
        console.log('准备调用 <加载> 的请求');
        var pageParams = {};
        pageParams.pageno = page.num || 1;
        pageParams.curpage = page.num || 1;
        pageParams.pagesize = t.refreshOption.pagesize;
        var netType = (t.refreshOption.moreUrl && t.refreshOption.moreUrl.length) ? (t.refreshOption.moreTypeGet ? 'get' : 'post') : (t.refreshOption.refreshTypeGet ? 'get' : 'post');
        var netApi = t.refreshOption.moreUrl || t.refreshOption.refreshUrl || '';

        var netParms = (t.refreshOption.moreUrl && t.refreshOption.moreUrl.length) ? t.refreshOption.moreParm : t.refreshOption.refreshParm;

        var newNetParms = app.assign(netParms, pageParams); //合并两个对象

        SLNetwork(netType, netApi, newNetParms, function(data) {
            console.log('完成调用 <加载> 的请求');
            endRefresh();
            if (data.code == 1) {

                if (data.curPage == 1 || data.curPage == 0) {
                    t.refreshOption.temptotalCount = data.totalCount
                } else {
                    data.totalCount = t.refreshOption.temptotalCount;
                    data.totalCount = data.totalCount ? data.totalCount : 0;
                }
                if (data.totalCount == 0) {
                    if (!data.data) {
                        // data.data={
                        //     length:0
                        // }
                        data.data = [];
                    }
                }
                if (t.refreshOption.more_cb) {
                    t.refreshOption.more_cb(t, data);
                } else if (t.refreshOption.refresh_cb) {
                    t.refreshOption.refresh_cb(t, data);
                }
            } else {
                data.data = [];
                t.refreshOption.refresh_cb(t, data);
                //联网失败的回调,隐藏下拉刷新和上拉加载的状态;
                t.MeRefScroll.endErr();
            }
        }, true);
    }

    //完成刷新和加载动作
    function endRefresh() {
        //结束下拉刷新和上拉加载
        //t.MeRefScroll.endErr();
    }

    function receiveNew(listHtml) {
        var listQuery = '#' + t.refreshOption.clearEmptyId;
        $(listQuery).append(listHtml);
    }

    t.appendHtml = function(contentHtml) {
        receiveNew(contentHtml);
    }

    t.successEndRef = function(curCount, totleCount) {
        t.MeRefScroll.endBySize(curCount, totleCount); //必传参数(当前页的数据个数, 总数据量)
    }

    return t;
}

/*MeRefScroll End*/

/*显示密码框*/
var isshow = true,
    rid = '';

function showpayinput(id, a) {
    Comm.bg(true);
    rid = $(a).attr("data-id")
    var data = JSON.parse(decodeURIComponent($(a).attr("data")));
    //{price:99,mark:mark,name:'dd'}
    //data.newroleid = (Comm.db('userinfo').roleid+1); //要升级的会元等级
    if (!data) return;
    marr = [];
    //1验证是否设置支付密码
    var user = Comm.db("userinfo");
    if (user.dealpassword == "0") {
        Comm.confirm("未设置支付密码去设置吧！", function(d) {
                if (d == 1) {
                    Comm.go('telBind.html');
                } else {
                    Comm.bg(false);
                }
                Comm.bg(false);
            })
            //Comm.showWindow('setPay', false)
        return;
    }

    var price = data.price;
    if (isshow) {
        isshow = false;
        $("body").append('<div id="' + id + '" class="_' + id + '" wtd="payTemp">\
				<img src="img/close.png" class="close" onclick="Comm.bg(false);Comm.showWindow();" />\
				<div class="f20 center bottom" id="' + id + 'name">' + ((data && data.name) ? (data.name) : ("请输入支付密码")) + '</div>\
				<div class="mart10 marb10">\
					<div><span style="font-size: 28px" id="' + id + 'price">' + price + '</span></div>\
					<div class="colorb5 mart10 marb20 realDiv"><label class=""> <span id="' + id + 'mark">' + data.mark + '</span>&nbsp;</label>\
					<input type="tel" onchange="getpwd(this)" number=""  maxlength="6" class="realInput" autofocus="autofocus"></div>\
					<div class="pwcontnet">\
						<input class="ipt1 ipt0" number type="password" maxlength="1" onkeyup="next(this)" />\
						<input class="ipt2 ipt0" number type="password" maxlength="1" onkeyup="next(this)" />\
						<input class="ipt3 ipt0" number type="password" maxlength="1" onkeyup="next(this)" />\
						<input class="ipt4 ipt0" number type="password" maxlength="1" onkeyup="next(this)" />\
						<input class="ipt5 ipt0" number type="password" maxlength="1" onkeyup="next(this)" />\
						<input class="ipt6 ipt0" number type="password" maxlength="1" onkeyup="next(this)" />\
					</div>\
				</div>\
				<div class="forgetpw"><span onclick="Comm.go(\'telBind.html?v=5\')">忘记密码?</span></div>\
			</div>');
        Comm.showWindow('payTemp', false);
    } else {
        Comm.showWindow('payTemp', false);
        $("#WTDBOX #" + id + "price").html(price);
        $("#WTDBOX #" + id + "name").html(data.name);
        $("#WTDBOX #" + id + "mark").html(data.mark);
        $("#WTDBOX #" + id + " input").attr("onkeyup", "next(this,'" + data.olduid + "')")
    }
    setTimeout(function() {
        $(".realInput").focus();
    }, 500);

    //切换
    var getpwd = function(t) {
        var vArr = (t.value).toString().split('');
        if (vArr.length <= 6) {
            $('.ipt0').val('');
            for (var i = 0, l = vArr.length; i < l; i++) {
                $('.ipt' + (i + 1)).val(vArr[i]);
            }
            if (vArr.length == 6) {
                //h回调函数
                nextbc && nextbc(t.value, rid);
            }
        } else {
            return false;
        }
    };
    window['getpwd'] = getpwd;

};

var marr = [];
var times = 1;
var nextbc = null;

function next(t, id) {
    if (isNaN(t.value)) {
        t.value = "";
        return;
    }
    if (t.value.length == 1) {
        if (t.nextElementSibling) {
            t.nextElementSibling.focus();
        }
        marr.push(t.value);
    }

    if (t.value.length == 0) {
        if (t.previousElementSibling) {
            t.previousElementSibling.focus();
        }
        marr.pop();
        console.log(marr.join(""));
    }
    if (marr.length == 6) {
        t.blur();
        var psw = marr.join("");
        //h回调函数
        var _rid = (Comm.db('userinfo').roleid + 1);
        nextbc && nextbc('', _rid, psw);
    }
}

//拼接Get接口和参数
function appendApi(api, parmas) {
    var string = api || '';
    string = string.length ? (api + '?') : '';
    for (var attr in parmas) {
        var param = attr + '=' + parmas[attr] + '&';
        string += param;
    }
    return string;
};

// 1 预定 2 竞拍  4 搭配销售 8 团购 16 店铺买赠 32 满减 64 预售 128 秒杀 256 积分
var active = {
    '预定': 1, //
    '竞拍': 2,
    '搭配销售': 4,
    '团购': 8, //
    '店铺买赠': 16,
    '满减': 32,
    '预售': 64, //
    '秒杀': 128, //
    '积分': 256
}

var hhmmss = {
    todate: function(date) {
        return new Date(date.replace(/-/g, "/").replace(/\.0/g, ""));
    },
    covert: function(ttime) {
        var tm = (ttime / 60 / 60).toFixed(4);
        var hh = parseInt(tm);
        tm = app.conunm(tm.toString().replace(/\d+\.(\d*)/, "$1") / 10000 * 60, 2);
        var mm = parseInt(tm);
        var ss = app.conunm(tm.toString().replace(/\d+\.(\d*)/, "$1") / 100 * 60, 0);
        //console.log(ss)
        if (hh < 10) {
            hh = "0" + hh;
        }
        if (mm < 10) {
            mm = "0" + mm;
        }
        if (ss < 10) {
            ss = "0" + ss;
        }
        return [hh, mm, ss];
    },
    init: function(listid, str) {
        setInterval(function() {
            $('#' + listid + ' .item').each(function(i, e) {
                var m = $(e).find('.hhmmss');
                if (m) {
                    var hms = m.attr('data') * 1;
                    //console.log(hms)
                    if (hms > 0) {
                        var r = hhmmss.covert(hms);
                        hms--;
                        if (hms >= 0) {
                            /*if (r[0] == "00") {
                                $(m).find('.hh').html(r[0]).hide();
                                $(m).find('.hh').next().hide();
                            } else {
                            }
                            if (r[1] == "00") {
                                $(m).find('.mm').html(r[1]).hide();
                                $(m).find('.mm').next().hide();
                            } else {
                            }*/
                            $(m).find('.hh').html(r[0]).show();
                            $(m).find('.mm').html(r[1]).show();
                            $(m).find('.ss').html(r[2]).show();
                        } else {
                            $(m).find('.hh').html("00").hide();
                            $(m).find('.hh').next().hide();
                            $(m).find('.mm').html("00").hide();
                            $(m).find('.mm').next().hide();
                            $(m).find('.ss').html("00").hide();
                            $(m).find('span:nth-child(n+2)').remove();
                            if (str) $(m).find('span').html(str);
                            else $(m).find('span').html("已结束");

                        }
                        m.attr('data', hms);
                    }
                }
            })
        }, 1000)
    }
}