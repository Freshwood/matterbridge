var MB = MB || {};

MB.core = {

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
        this.vue = new Vue({
                               el: '#contentVue',
                               data: {
                                   navigation: 'HOME'
                               },
                               methods: {
                                   switchNavigation: function (newNav) {
                                       this.navigation = newNav;
                                   }
                               }
                           });
    }
};