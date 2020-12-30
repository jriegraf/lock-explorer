import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    userId: "",
    nextPanelId: 4,
    panels: [],
    availableTables: [],
    availableViews: [],
    sessions: []
  },

  getters: {
    getUserId: state => {
      return state.userId;
    },
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
    },
    getViewList: state => {
      console.log("getViewList: " + JSON.stringify(state.availableViews));
      return state.availableViews;
    },
    getSessions: state => {
      console.log("getSessions: " + JSON.stringify(state.sessions));
      return state.sessions;
    }
  },

  mutations: {
    setUserId(state, id) {
      state.userId = id;
      console.debug("STATE CHANGED:\n" + JSON.stringify(state, null, 2));
    },

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
      console.debug("STATE CHANGED:\n" + JSON.stringify(state, null, 2));
    },

    removePanel(state, panelId) {
      state.panels = state.panels.filter(p => p.panelId != panelId);
      console.debug("STATE CHANGED:\n" + JSON.stringify(state, null, 2));
    },

    addTable(state, table) {
      state.tables.push(table);
      console.debug("ADD TABLE:\n" + JSON.stringify(state, null, 2));
    },
    setAvailableTables(state, tables) {
      if (!Array.isArray(tables)) {
        console.err("tables is not an array.");
      } else {
        state.availableTables = tables;
        console.debug(
          "SET AVAILABLE TABLES:\n" + JSON.stringify(state, null, 2)
        );
      }
    },
    setAvailableViews(state, views) {
      if (!Array.isArray(views)) {
        console.err("views is not an array.");
      } else {
        state.availableViews = views;
        console.debug(
          "SET AVAILABLE VIEWS:\n" + JSON.stringify(state, null, 2)
        );
      }
    },
    setSessions(state, sessions) {
      if (!Array.isArray(sessions)) {
        console.err("sessions is not an array.");
      } else {
        state.sessions = sessions.sort();
        console.debug("SET SESSIONS:\n" + JSON.stringify(state, null, 2));
      }
    }
  },

  actions: {}
});
