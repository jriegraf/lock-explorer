import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import vuetify from "./plugins/vuetify";
import axios from "axios";

Vue.config.productionTip = false;

document.title = "LockExplorer";
const API_URL =
  "http://" +
  (window.location.host.split(":")[0] === "localhost"
    ? "localhost"
    : "lock-explorer.syslab.in.htwg-konstanz.de") +
  ":8080/";

console.log("API_URL: " + API_URL);

const mixin = {
  methods: {
    queryApi: function(body) {
      console.info("querApi with body: \n" + JSON.stringify(body));

      const config = {
        method: "post",
        url: API_URL + "message",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json"
        },
        data: body
      };

      return axios(config);
    },

    getAvailableTables: function() {
      this.queryApi({
        type: "GET_AVAILABLE_TABLES",
        user: this.$store.getters.getUserId
      })
        .then(result =>
          this.$store.commit(
            "setAvailableTables",
            result.data.payload.availableTables
          )
        )
        .catch(e => console.error("Error: " + JSON.stringify(e.response.data)));
    },

    getAvailableViews: function() {
      this.queryApi({
        type: "GET_AVAILABLE_VIEWS",
        user: this.$store.getters.getUserId
      })
        .then(result => {
          console.log(JSON.stringify(result, null, 2));
          this.$store.commit(
            "setAvailableViews",
            result.data.payload.availableViews
          );
        })
        .catch(e => console.error("Error: " + JSON.stringify(e.response)));
    },

    getSessions: function() {
      return this.queryApi({
        type: "GET_SESSIONS",
        user: this.$store.getters.getUserId
      })
        .then(result => {
          this.$store.commit("setSessions", result.data.payload.sessions);
          return result.data.payload.sessions;
        })
        .catch(e => console.error("Error: " + JSON.stringify(e.response)));
    }
  }
};

new Vue({
  mixins: [mixin],
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount("#app");
