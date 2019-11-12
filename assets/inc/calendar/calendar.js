var Calendar = {
    size: {},
    option: null,
    show: function (option) {
        this.option = option;
        
        var calendarDate = new Date();
        this.option.d = {
            y: calendarDate.getFullYear(),
            m: calendarDate.getMonth() + 1,
            d: calendarDate.getDate()
        };
        this.getNowInfo();
        if(!option.size) this.isPc();
        else this.size = option.size;
        this.showDate();
    },
    showDate: function () {
        this.showCalenderNav();
        this.showCalenderHeader();
        this.showCalenderMain();
    },
    getNowInfo: function () {
        var t = this.option.d;
        t.days = this.GetDate(t.y, t.m);
        t.startDay = this.GetDay(t.y, t.m);
        t.lastMonthDays = this.lastMonthDays(t.y, t.m);
    },
    GetDate: function (year, month) {
        var d = new Date(year, month, 0);
        return d.getDate();
    },
    GetDay: function (year, month) {
        var d = new Date(year, month - 1, 1);
        return d.getDay();
    },
    lastMonthDays: function (year, month) {
        if (month == "1") {
            year = year - 1;
            month = 12;
        } else {
            month = month - 1;
        }
        var d = new Date(year, month, 0);
        return d.getDate();
    },
    showCalenderNav: function () {
        var nStr = "<div id='CalenderNav' style='text-align:center'></div>";
        if ($("#" + this.option.containerId).find("#CalenderNav").length == "0") $("#" + this.option.containerId).append(nStr);
        var d = this.joinYM();
        var str = '<div class="t-a-c paddt15 paddb15">' + '<img src="../inc/calendar/img/fanhui.png" alt="" onclick="Calendar.lastMonth()">' + '<span class="f16 paddl10 paddr10">' + d + "</span>" + '<img src="../inc/calendar/img/fanhui.png" alt="" style="transform: rotateY(180deg)" onclick="Calendar.nextMonth()">' + " </div>";
        $("#CalenderNav").html(str);
    },
    showCalenderHeader: function () {
        var hStr = "<div id='CalenderHeader' style='text-align:center;display: flex;justify-content: space-around;'></div>";
        var s = this.size;
        $("#" + this.option.containerId).append($(hStr));
        var arr = [{
            i: "0",
            text: "日"
        }, {
            i: "1",
            text: "一"
        }, {
            i: "2",
            text: "二"
        }, {
            i: "3",
            text: "三"
        }, {
            i: "4",
            text: "四"
        }, {
            i: "5",
            text: "五"
        }, {
            i: "6",
            text: "六"
        }];
        var str = "";
        for (var i = 0; i < arr.length; i++) {
            str += "<span style='display:inline-block;width:" + s.w + "px;height:" + s.w + "px;line-height:" + s.w + "px;text-align:center;color:#C2C2C2;font-size:16px;margin:2px 2px' data='" + arr[i].i + "'>" + arr[i].text + "</span>";
        }
        $("#CalenderHeader").html(str);
    },
    showCalenderMain: function () {
        var mStr = "<div id='CalenderMain' style='text-align:left;display: flex;justify-content: space-around;flex-wrap: wrap;'></div>";
        var d = this.option.d;
        var s = this.size;
        var arr = [];
        var c = this.isAddClass();
        if ($("#" + this.option.containerId).find("#CalenderMain").length == "0") $("#" + this.option.containerId).append($(mStr));
        if (d.startDay != "0") {
            for (var i = 0; i < d.startDay; i++) {
                var t = this.getYmd(1, i);
                var str = "<span class='invalid " + c.l[i] + "' name='invalid'  style='display:inline-block;width:" + s.w + "px;height:" + s.w + "px;line-height:" + s.w + "px;text-align:center;color:#C2C2C2;font-size:16px;margin:2px 2px' data='" + t.full + "'>" + t.value + "</span>";
                arr.unshift(str);
            }
        }
        for (var i = 0; i < d.days; i++) {
            var t = this.getYmd(2, i);
            var str = "<span onclick='Calendar.click(this)' class='effective " + c.n[i] + "' name='effective' style='display:inline-block;width:" + s.w + "px;height:" + s.w + "px;line-height:" + s.w + "px;text-align:center;color:block;font-size:16px;margin:2px 2px' data='" + t.full + "'>" + t.value + "</span>";
            arr.push(str);
        }
        var _diff = 42 - (d.startDay + d.days);
        if (d.startDay + d.days < 42) {
            for (var i = 0; i < _diff; i++) {
                var t = this.getYmd(3, i);
                var str = "<span class='invalid " + c.f[i] + "' name='invalid' style='display:inline-block;width:" + s.w + "px;height:" + s.w + "px;line-height:" + s.w + "px;text-align:center;color:#C2C2C2;font-size:16px;margin:2px 2px' data='" + t.full + "'>" + t.value + "</span>";
                arr.push(str);
            }
        }
        $("#CalenderMain").html(arr.join(""));
    },
    resetClass: function (i) {
        if (this.option.data) {
            var _d = this.option.data;
            console.info(_d);
        }
    },
    getYmd: function (t, i) {
        var d = this.option.d;
        var str = "";
        var _y = _m = _d = 0;
        if (t == "1") {
            if (d.m == 1) {
                if (d.startDay != 0) {
                    _y = d.y - 1;
                    _m = 12;
                    _d = d.lastMonthDays - i;
                    str = _y + "," + _m + "," + _d;
                }
            } else {
                _y = d.y;
                _m = d.m - 1;
                _d = d.lastMonthDays - i;
                str = _y + "," + _m + "," + _d;
            }
        }
        if (t == "2") {
            _y = d.y;
            _m = d.m;
            _d = i + 1;
            str = _y + "," + _m + "," + _d;
        }
        if (t == "3") {
            if (d.m == "12") {
                _y = d.y + 1;
                _m = 1;
            } else {
                _y = d.y;
                _m = d.m + 1;
            }
            _d = i + 1;
            str = _y + "," + _m + "," + _d;
        }
        return {
            full: str,
            value: _d
        };
    },
    isPc: function () {
       var xx = window.screen.availWidth;
        this.size.w = ((xx - 28) / 7).toFixed(2);
    },
    isAddClass: function () {
        var d = this.option.d;
        var c = this.option.addClass;
        var sc = this.option.stateClass;
        var scarr = [];
        var r = {
            invalid: "",
            effective: ""
        };
        var arr = {
            l: [],
            n: [],
            f: []
        };
        if (c) {
            if (c.invalid) r.invalid = c.invalid;
            else r.invalid = "";
            if (c.effective) r.effective = c.effective;
            else r.effective = "";
        }
        if (d.startDay != "0") {
            for (var i = 0; i < d.startDay; i++) {
                arr.l.push(r.invalid);
            }
        }
        for (var i = 0; i < d.days; i++) {
            arr.n.push(r.effective);
        }
        var _diff = 42 - (d.startDay + d.days);
        if (d.startDay + d.days < 42) {
            for (var i = 0; i < _diff; i++) {
                arr.f.push(r.invalid);
            }
        }
        if (sc) {
            sc.forEach(function (v, i) {
                var _i = sc[i].i * 1 - 1;
                var _s = arr.n[_i];
                arr.n[_i] = _s + " " + v.class;
            });
        }
        return arr;
    },
    lastMonth: function () {
        if (this.option.d.m * 1 == "1") {
            this.option.d.y += -1;
            this.option.d.m = 12;
        } else {
            this.option.d.m += -1;
        }
        Calendar.getNowInfo();
        Calendar.showCalenderNav();
        Calendar.showCalenderMain();
        this.option.callback.last && this.option.callback.last( this.option.d);
    },
    nextMonth: function () {
        this.option.stateClass=[];
        if (this.option.d.m * 1 == "12") {
            this.option.d.y += 1;
            this.option.d.m = 1;
        } else {
            this.option.d.m += 1;
        }
        Calendar.getNowInfo();
        Calendar.showCalenderNav();
        Calendar.showCalenderMain();
        this.option.callback.next && this.option.callback.next( this.option.d);
    },
    joinYM: function () {
        var d = this.option.d;
        if (d.m < 10) var _m = "0" + Number(d.m);
        else var _m = Number(d.m);
        return d.y + "年" + _m + "月";
    },
    click: function (a) {
        this.option.callback.click && this.option.callback.click(a);
    }
};