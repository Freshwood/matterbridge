var MB = MB || {
        errorHandler: function (event, jqxhr, settings, thrownError) {
            console.log("Could not communicate: " + thrownError);
        }
    };

Vue.component('landing', {
    template: '#home-template',
    props: {
        url: {
            type: String,
            required: true
        }
    },
    data: function () {
        return {
            text: "This is a sample text",
            categories: [],
            ws: {},
            overallStatus: {}
        }
    },
    methods: {
        saveCategories: function (categories) {
            this.categories = categories;
            console.log(categories);
        },
        onOpen: function() {
            console.log("web socket connection open");
            this.ws.send("Start");
        },
        onClose: function() {
            console.log("socket closed");
        },
        onMessage: function(msg) {
            console.log(msg.data);

            var jsonData = {};

            try {
                jsonData = JSON.parse(msg.data);
                this.overallStatus = jsonData;
            } catch(ex) {
                console.error(ex);
            }
        },
        connectToSocket: function() {
            this.ws = new WebSocket(MB.core.wsUrl);

            this.ws.onopen = this.onOpen;

            this.ws.onmessage = this.onMessage;

            this.ws.onclose = this.onClose;
        }
    },
    created: function () {
        var vm = this;
        $.get({
                  url: vm.url + 'category',
                  success: vm.saveCategories
              });

        vm.connectToSocket();
    }
});

MB.core = {

    wsUrl: "ws://" + location.host + "/socket",

    navs: {
        home: "HOME",
        nineGag: "9GAG",
        codingLove: "CODING_LOVE",
        rss: "RSS",
        bot: "BOT"
    },

    /**
     * The root vue instance
     * Navigation: HOME, 9GAG, CODING_LOVE, RSS, BOT
     */
    init: function () {
        var navs = MB.core.navs;
        this.vue = new Vue({
                               el: '#contentVue',
                               data: {
                                   navigation: 'HOME',
                                   url: '/'
                               },
                               methods: {
                                   switchNavigation: function (newNav) {
                                       this.navigation = newNav;
                                   },
                                   isHome: function () {
                                       return this.navigation === navs.home;
                                   },
                                   isNineGag: function () {
                                       return this.navigation === navs.nineGag;
                                   },
                                   isCodingLove: function () {
                                       return this.navigation === navs.codingLove;
                                   },
                                   isRss: function () {
                                       return this.navigation === navs.rss;
                                   },
                                   isBot: function () {
                                       return this.navigation === navs.bot;
                                   }
                               }
                           });
    }
};