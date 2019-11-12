var Foot = new function () {
    var list = [{
            cls: 'index',
            name: '商家'
        },
        {
            cls: 'my',
            name: '我的'
        }
    ];


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
            oLi.style.backgroundImage = "url(./img/" + ((check(_.cls + ".htm") ? _.cls + "-r" : _.cls)) + ".png)"
            oLi.innerHTML = _.name;
            frag.appendChild(oLi);
        }
        oUL.appendChild(frag);
        $('.shared').html(oUL);
        Comm.resizeSection();
    }
};

			