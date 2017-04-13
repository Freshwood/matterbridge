var MB = MB || {
        errorHandler: function (event, jqxhr, settings, thrownError) {
            console.log("Could not communicate: " + thrownError);
        },
        toDateFilter: function (value) {
            var retVal = '';
            if (!value) {
                return retVal;
            }
            try {
                var date = new Date(value);
            } catch (ex) {
                return retVal;
            }
            return date.toLocaleDateString();
        },
        apiLocation: "http://" + window.location.host + "/api/matterbridge"
    };

Vue.component('bot-config', {
    template: '#bot-config-template',
    props: {
        url: {
            type: String,
            required: true
        }
    },
    data: function () {
        return {
            bots: [],
            deletedBots: [],
            botResources: [],
            activeBotId: '',
            hoverBotId: '',
            botName: '',
            resourceValue: '',
            showDeletedBots: false,
            canBotBeCreated: false,
            isWaitForExistingCheck: false
        }
    },
    methods: {
        loadBots: function () {
            var vm = this;
            $.get({url: vm.url + "bot/", success: vm.storeBots});
            vm.loadDeletedBots();
        },
        loadDeletedBots: function () {
            var vm = this;
            $.get({url: vm.url + "bot/deleted", success: vm.storeDeletedBots});
        },
        restoreBot: function (botId) {
            var vm = this;
            $.get({url: vm.url + "bot/restore/" + botId, success: vm.loadBots});
        },
        loadResources: function () {
            var vm = this;
            $.get({url: vm.url + "bot/resources/" + vm.activeBotId, success: vm.storeResources});
        },
        storeBots: function (bots) {
            this.bots = bots;
        },
        storeDeletedBots: function (bots) {
            this.deletedBots = bots;
        },
        storeResources: function (resources) {
            this.botResources = resources;
        },
        updateBotId: function (botId) {
            if (this.activeBotId !== botId) {
                this.activeBotId = botId;
                this.loadResources();
            }
        },
        activeBody: function (botId) {
            return botId === this.activeBotId;
        },
        hoverBody: function (botId) {
            return botId === this.hoverBotId;
        },
        saveResource: function () {
            var vm = this;
            if (this.activeBotId !== undefined && this.isInputValid()) {
                $.post(
                    {
                        url: vm.url + 'bot/add',
                        contentType: 'application/json',
                        data: JSON.stringify({botId: vm.activeBotId, name: vm.resourceValue}),
                        success: vm.loadResources
                    });
            }
        },
        isInputValid: function () {
            return this.resourceValue.length > 5;
        },
        addNewBot: function () {
            var vm = this;
            if (vm.canBotBeCreated) {
                $.post(
                    {
                        url: vm.url + 'bot/add',
                        contentType: 'application/json',
                        data: JSON.stringify({name: vm.botName}),
                        success: vm.loadBots
                    });
                vm.canBotBeCreated = false;
            }
        },
        updateHoverId: function (botId) {
            this.hoverBotId = botId;
        },
        clearHoveId: function () {
            this.hoverBotId = '';
        },
        deleteBot: function (botId) {
            var vm = this;
            if (vm.hoverBotId === vm.activeBotId) {
                $.ajax({url: vm.url + 'bot/' + botId, type: 'DELETE', success: vm.loadBots});
            }
        },
        deleteResource: function (resourceId) {
            var vm = this;
            $.ajax({url: vm.url + 'bot/resources/' + resourceId, type: 'DELETE', success: vm.loadResources});
        },
        toggleDeletedBotsVisibility: function () {
            this.showDeletedBots = !this.showDeletedBots;
        }
    },
    computed: {
        resources: function () {
            var vm = this;
            return this.botResources.filter(function (element) {
                return element.botId === vm.activeBotId;
            });
        }
    },
    watch: {
      botName: function (value) {
          var vm = this;
          vm.canBotBeCreated = false;

          clearTimeout(vm.timer);

          if (value.length > 3) {
              // Just a debounce
              vm.timer = setTimeout(function () {
                  $.get({url: vm.url + 'bot/exists/' + value, success: function (response) {
                      if (response === 'false') {
                          vm.canBotBeCreated = true;
                      }
                  }});
                  vm.isWaitForExistingCheck = false;
              }, 500);
              vm.isWaitForExistingCheck = true;
          } else {
              vm.isWaitForExistingCheck = false;
          }
      }
    },
    filters: {
        toDate: MB.toDateFilter
    },
    created: function () {
        this.loadBots();
    }
});

Vue.component('rss-config', {
    template: '#rss-config-template',
    props: {
        url: {
            type: String,
            required: true
        }
    },
    data: function () {
        return {
            rssEntries: [],
            rssModel: {
                name: 'RSS name',
                rssUrl: "RSS url",
                incomingToken: '...'
            }
        }
    },
    methods: {
        loadRssEntries: function () {
            var vm = this;
            $.get({url: vm.url + "rss/", success: vm.storeRssEntries});
        },
        storeRssEntries: function (rssEntries) {
            this.rssEntries = rssEntries;
        },
        submitForm: function () {
            var vm = this;
            $.post({
                       url: vm.url + 'rss/add',
                       contentType: 'application/json',
                       data: JSON.stringify(vm.rssModel),
                       success: vm.rssEntryAdded
                   });
        },
        rssEntryAdded: function () {
            this.loadRssEntries();
        }
    },
    computed: {
        formOk: function () {
            var model = this.rssModel;
            return model.name.length > 3 && model.rssUrl.indexOf('http') !== -1 && model.incomingToken.length > 5;
        }
    },
    filters: {
        toDate: MB.toDateFilter,
        truncate: function (value) {
            var retVal = value;

            if (retVal.length > 25) {
                retVal = retVal.substring(0, 25) + '...';
            }

            return retVal;
        }
    },
    created: function () {
        this.loadRssEntries();
    }
});

Vue.component('coding-love-config', {
    template: '#coding-love-config-template',
    props: {
        url: {
            type: String,
            required: true
        }
    },
    data: function () {
        return {
            location: MB.apiLocation,
            lastGifs: []
        }
    },
    methods: {
        storeCodingLoveGifs: function (gifs) {
            this.lastGifs = gifs;
        }
    },
    filters: {
        toDate: MB.toDateFilter
    },
    created: function () {
        var vm = this;
        $.get({url: vm.url + "codingLove/last", success: vm.storeCodingLoveGifs});
    }
});

Vue.component('ninegag-config', {
    template: '#ninegag-config-template',
    props: {
        url: {
            type: String,
            required: true
        }
    },
    data: function () {
        return {
            location: MB.apiLocation,
            lastGifs: []
        }
    },
    methods: {
        loadNineGagGifs: function () {
            var vm = this;
            $.get({url: vm.url + "9gag/last", success: vm.storeLastNineGagGifs});
        },
        storeLastNineGagGifs: function (gifs) {
            this.lastGifs = gifs;
        },
        deleteNineGagGif: function (gifId) {
            var vm = this;
            $.ajax({url: vm.url + '9gag/' + gifId, method: 'DELETE', success: vm.loadNineGagGifs});
        }
    },
    filters: {
        toDate: MB.toDateFilter
    },
    created: function () {
        this.loadNineGagGifs();
    }
});

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
            categories: [],
            ws: {},
            nineGagCount: 0,
            codingLoveCount: 0,
            rssCount: 0,
            botCount: 0,
            categoryCount: 0,
            showBold: false
        }
    },
    methods: {
        saveCategories: function (categories) {
            this.categories = categories;
            console.log(categories);
        },
        onOpen: function () {
            console.log("web socket connection open");
            this.ws.send("Start");
        },
        onClose: function () {
            console.log("socket closed");
        },
        onMessage: function (msg) {
            console.log(msg.data);

            var jsonData = {};

            try {
                jsonData = JSON.parse(msg.data);
                this.nineGagCount = jsonData.nineGagCount;
                this.codingLoveCount = jsonData.codingLoveCount;
                this.rssCount = jsonData.rssCount;
                this.botCount = jsonData.botCount;
                this.categoryCount = jsonData.categoryCount;
            } catch (ex) {
                console.error(ex);
            }
        },
        connectToSocket: function () {
            this.ws = new WebSocket(MB.core.wsUrl);

            this.ws.onopen = this.onOpen;

            this.ws.onmessage = this.onMessage;

            this.ws.onclose = this.onClose;
        }
    },
    watch: {
        nineGagCount: function () {
            var vm = this;
            vm.showBold = true;
            setTimeout(function () {
                vm.showBold = false;
            }, 1000);
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