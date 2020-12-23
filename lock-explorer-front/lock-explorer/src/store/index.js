import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

const API_URL = "http://localhost:8080/";

export default new Vuex.Store({
  state: {
    nextPanelId: 4,
    panels: [{ panelId: 0, type: "Editor", name: "987" }],
    availableTables: []
  },

  getters: {
    getTableData: state => tabName => {
      let ret = state.tables.filter(table => table.tableName == tabName);
      if (ret.length > 1) throw "Multiple tables with same name!";
      return ret[0];
    },
    getPanels: state => {
      return state.panels;
    },
    getTableList: state => {
      console.log("getTableList: " + JSON.stringify(state.availableTables));
      return state.availableTables;
    }
  },

  mutations: {
    addPanel(state, panel) {
      let dupPanel = state.panels.find(
        p => p.type == panel.type && p.name == panel.name
      );
      if (dupPanel != undefined) {
        state.panels = state.panels.filter(p => p.panelId != dupPanel.panelId);
      }
      panel.panelId = state.nextPanelId;
      state.nextPanelId++;
      state.panels.unshift(panel);
      console.debug("STATE CHANGED:\n" + JSON.stringify(state));
    },
    removePanel(state, panelId) {
      state.panels = state.panels.filter(p => p.panelId != panelId);
      console.debug("STATE CHANGED:\n" + JSON.stringify(state));
    },
    addTable(state, table) {
      console.debug("ADD TABLE:\n" + JSON.stringify(state));
      state.tables.push(table);
    },
    setAvailableTables(state, tables) {
      if (!Array.isArray(tables)) {
        console.err("tables is not an array.");
      } else {
        state.availableTables = tables;
      }
    }
  },

  actions: {
    async fetchTables({ commit }) {
      const header = { Accept: "application/json" };
      const url = API_URL + "getTables/";

      fetch(url, { headers: header })
        .then(response => {
          if (response.status == 200) {
            response.json().then(tables => {
              console.log("Fetched tables: " + JSON.stringify(tables.data.map(x => x.TABLE_NAME)));
              commit("setAvailableTables", tables.data.map(x => x.TABLE_NAME));
            });
          } else {
            console.error("Can not fetch tables");
          }
        })
        .catch(error => console.error("Can not fetch tables", error));
    }
  },

  modules: {}
});
