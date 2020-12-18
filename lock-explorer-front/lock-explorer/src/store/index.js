import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    nextPanelId: 4,
    joke: "",
    panels: [
      { panelId: 0, type: "Editor", sid: "987" },
      { panelId: 1, type: "Table", table_name: "TabA" },
      { panelId: 2, type: "Editor", sid: "1234" },
      { panelId: 3, type: "Table", table_name: "Angestellte" },
    ],
    tables: [
      {
        tableName: "TabA",
        data: [
          {
            name: "Frozen Yogurt",
            calories: 159,
            fat: 6.0,
            carbs: 24,
            protein: 4.0,
            iron: "1%",
          },
          {
            name: "Ice cream sandwich",
            calories: 237,
            fat: 9.0,
            carbs: 37,
            protein: 4.3,
            iron: "1%",
          },
          {
            name: "Eclair",
            calories: 262,
            fat: 16.0,
            carbs: 23,
            protein: 6.0,
            iron: "7%",
          },
          {
            name: "Cupcake",
            calories: 305,
            fat: 3.7,
            carbs: 67,
            protein: 4.3,
            iron: "8%",
          },
          {
            name: "Gingerbread",
            calories: 356,
            fat: 16.0,
            carbs: 49,
            protein: 3.9,
            iron: "16%",
          },
          {
            name: "Jelly bean",
            calories: 375,
            fat: 0.0,
            carbs: 94,
            protein: 0.0,
            iron: "0%",
          },
          {
            name: "Lollipop",
            calories: 392,
            fat: 0.2,
            carbs: 98,
            protein: 0,
            iron: "2%",
          },
        ],
        headers: [
          {
            text: "Dessert (100g serving)",
            align: "start",
            sortable: false,
            value: "name",
          },
          { text: "Calories", value: "calories" },
          { text: "Fat (g)", value: "fat" },
          { text: "Carbs (g)", value: "carbs" },
          { text: "Protein (g)", value: "protein" },
          { text: "Iron (%)", value: "iron" },
        ],
      },
      {
        tableName: "Angestellte",
        data: [{ name: "Julian" }, { name: "Fabian" }, { name: "Joachim" }],
        headers: [{ text: "Name", value: "name" }],
      },
    ],
  },
  getters: {
    getTableDataA: (state) => (tabName) => {
      let ret = state.tables.filter((table) => table.tableName == tabName);
      if (ret.length > 1) throw "Multiple tables with same name!";
      return ret[0];
    },
    getPanels: (state) => {
      return state.panels;
    },
    getJoke: (state) => {
      return state.joke;
    },
  },
  mutations: {
    addPanel(state, panel) {
      panel.panelId = state.nextPanelId;
      state.nextPanelId++;
      state.panels.push(panel);
      console.log("STATE CHANGED:\n" + JSON.stringify(state));
    },
    removePanel(state, panelId) {
      state.panels = state.panels.filter((p) => p.panelId != panelId);
      console.log("STATE CHANGED:\n" + JSON.stringify(state));
    },
    updateJoke(state, joke) {
      state.joke = joke;
      console.log("Joke updated.");
    },
  },
  actions: {
    async fetchJokes({ commit }) {
      var myHeaders = new Headers();
      myHeaders.append("Accept", "application/json");
      const header = { Accept: "application/json" };
      const url = "https://icanhazdadjoke.com/";

      let joke = await fetch(url, { headers: header })
        .then((response) => response.json())
        .then((result) => result.joke)
        .catch((error) => console.log("error", error));

      console.log("Fetched joke: " + joke);
      commit("updateJoke", joke);
    },
  },
  modules: {},
});
