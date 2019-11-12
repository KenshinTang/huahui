//底部v  Foot.init 渲染
var Foot = new function () {
    var list = [{
        cls: 'index',
        name: '首页'
    },
    {
        cls: 'class',
        name: '分类'
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

    this.init = function () {
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
        $('.shared').append(oUL);
        Comm.resizeSection();
    }
};

//底部v  Foot.init 渲染
var Foots = new function () {
    var list = [{
        cls: 'shopMain',
        name: '全部商品'
    },
    {
        cls: 'shopClass',
        name: '商品分类'
    },
    {
        cls: 'shopMember',
        name: '会员中心'
    },
    {
        cls: 'shopContact',
        name: '联系商家'
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

    this.init = function (bussinessId) {
        //Comm.setAndroidHome();
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
            if (_.cls == "shopContact") {

            } else {
                oLi.setAttribute('onclick', 'Comm.goself(\'' + _.cls + '.html?id=' + bussinessId + '\')');
            }

            oLi.style.backgroundImage = "url(img/shop/" + ((check(_.cls + ".htm") ? _.cls + "-r" : _.cls)) + ".png)"
            oLi.innerHTML = _.name;
            frag.appendChild(oLi);
        }
        oUL.appendChild(frag);
        $('.shared').html(oUL);
        Comm.resizeSection();
    }
};

//收藏 
var Coll = new function () {
    this.user = Comm.db('userinfo');
    //opt goodsId  商品id  type：1：商品 2：商家
    this.addCollection = function (opt, cb) {
        AJAX.POST('/common/collection/addCollection', { customerId: this.user.customerId, goodsId: opt.goodsId, type: opt.type }, function (d) {
            if (d.code == 1) {
                cb && cb(d)
            } else {
                Comm.message(d.msg)
            }
        })
    }
    this.delCollection = function (id, cb) {
        AJAX.POST('/common/collection/delCollection', { collectionId: id }, function (d) {
            if (d.code == 1) {
                cb && cb()
            } else {
                Comm.message(d.msg)
            }
        })
    }
}

//本地购物车
var Cart = new function () {
    var ct = Comm.db('cart') ? Comm.db('cart') : [];
    this.mycart = ct;
    this.save = function () {
        Comm.db('cart', ct)
    }
    /*
        opt={
            num:num,
            ifFlowerSuper:ifFlowerSuper,
            bussinessId:bussinessId,
            goodsId:goodsId,
            goodsSkuId:goodsSkuId,
        }
    */
    this.add = function (opt) {
        var i = Cart.query(opt.goodsId, opt.goodsSkuId);
        if (i >= 0) {
            if (opt.isadd) {
                ct[i].num = ct[i].num * 1
                ct[i].num += opt.num * 1;//累加
            } else {
                ct[i].num = opt.num * 1;//替换
            }
            if (ct[i].num <= 0) {
                ct[i].num = 0;
            }
        } else {
            ct.push(opt)
        }
        this.save();
    }

    this.query = function (goodsId, goodsSkuId) {
        for (var i = 0; i < ct.length; i++) {
            var e = ct[i];
            if (e.goodsId == goodsId && e.goodsSkuId == goodsSkuId) {
                return i;
            }
        }
        return -1;
    }

    this.getgoodsSkuIds = function () {
        var r = [];
        if (ct) {
            for (var i = 0; i < ct.length; i++) {
                var e = ct[i];
                r.push(e.goodsSkuId);
            }
        }
        return r;
    }
}