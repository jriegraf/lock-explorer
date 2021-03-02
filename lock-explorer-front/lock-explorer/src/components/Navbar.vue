<template>
  <v-navigation-drawer permanent app dark>
    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="title text-uppercase">
          <h1>
            <span class="font-weight-light">Lock</span>
            <span>Explorer</span>
          </h1>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>

    <v-divider />

    <v-list dense>
      <v-list-item>
        <v-menu open-on-hover top :offset-x="offset">
          <template v-slot:activator="{ on, attrs }">
            <v-btn color="primary" small block dark v-bind="attrs" v-on="on">
              <v-icon class="mr-2">mdi-code-braces</v-icon>
              Sessions
            </v-btn>
          </template>

          <v-list>
            <v-list-item-group max="0">
              <v-card-actions v-for="item in getSessions" :key="item">
                <v-icon>mdi-menu-right</v-icon>
                <v-list-item-title v-text="item"></v-list-item-title>
                <v-btn icon @click="addEditor(item)">
                  <v-icon>mdi-open-in-new</v-icon>
                </v-btn>
                <v-btn icon @click="closeSession(item)">
                  <v-icon>mdi-trash-can</v-icon>
                </v-btn>
              </v-card-actions>
              <v-divider></v-divider>
              <v-list-item @click="openSession">
                <v-list-item-icon>
                  <v-icon>mdi-plus</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <span>Add Session</span>
                </v-list-item-content>
              </v-list-item>
            </v-list-item-group>
          </v-list>
        </v-menu>
      </v-list-item>
      <v-list-item>
        <v-menu top :offset-x="offset">
          <template v-slot:activator="{ on, attrs }">
            <v-btn color="primary" small block dark v-bind="attrs" v-on="on">
              <v-icon class="mr-2">mdi-file-code</v-icon>
              Load Scenario
            </v-btn>
          </template>

          <v-list dense>
            <v-list-item-group max="0" v-model="selectedItem" color="primary">
              <v-list-item v-for="(item, i) in scenario_items" :key="i" @click="openScenario(item.title)">
                <v-list-item-icon>
                  <v-icon>mdi-menu-right</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <span v-text="item.title"></span>
                </v-list-item-content>
              </v-list-item>
            </v-list-item-group>
          </v-list>
        </v-menu>
      </v-list-item>

      <v-divider class="my-2" />

      <v-list-group v-if="showTable" prepend-icon="mdi-table">
        <template v-slot:activator>
          <v-list-item-title>Tables</v-list-item-title>
        </template>

        <v-list-item
          v-for="table in tables"
          :key="table.name"
          @click="openTable(table)"
        >
          <v-icon>mdi-menu-right</v-icon>
          <v-icon class="mr-2" color="red" v-if="table.locked === 'yes'">
            mdi-lock
          </v-icon>
          <span class="text-lowercase" v-text="table.name"></span>
        </v-list-item>
      </v-list-group>

      <v-list-item v-else>
        <v-list-item-icon>
          <v-icon>mdi-table</v-icon>
        </v-list-item-icon>
        <v-list-item-title>
          No Tables available
        </v-list-item-title>
      </v-list-item>

      <v-list-group v-if="showViews" prepend-icon="mdi-table">
        <template v-slot:activator>
          <v-list-item-title>Views</v-list-item-title>
        </template>
        <v-list-item
          small
          v-for="title in views"
          :key="title"
          @click="openView(title)"
        >
          <v-icon>mdi-menu-right</v-icon>
          <v-list-item-title v-text="title"></v-list-item-title>
        </v-list-item>
      </v-list-group>
      <v-list-item v-else>
        <v-list-item-icon>
          <v-icon>mdi-table</v-icon>
        </v-list-item-icon>
        <v-list-item-title>
          No Views available
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
export default {
  data: () => ({
    selectedItem: "",
    scenario_items: [{ title: "Lost Update" }, { title: "Deadlock" }],
    offset: true,
    tables: [],
    views: []
  }),

  methods: {
    addEditor: function(sid) {
      console.log("addEditor");
      let editor = { type: "Editor", name: sid };
      this.$store.commit("addPanel", editor);
      this.session = "";
    },
    openTable: function(table) {
      let tableObj = { type: "Table", table: table };
      console.log("openTable: " + JSON.stringify(tableObj));
      this.$store.commit("addPanel", tableObj);
    },
    openView: function(viewName) {
      let view = { type: "View", name: viewName };
      console.log("openView: " + JSON.stringify(view));
      this.$store.commit("addPanel", view);
    },
    openScenario: function(scenarioName) {
      let scenario = { type: "Scenario", name: scenarioName };
      console.log("openScenario: " + JSON.stringify(scenario));
      this.$store.commit("addPanel", scenario);
    },
    async openSession() {
      await this.$root
        .openSession()
        .then(newSid => this.addEditor(newSid))
        .catch(err => console.error("Can not open session.\n" + err));
    },
    closeSession(sid) {
      this.$root
        .queryApi({
          type: "CLOSE_SESSION",
          user: this.$store.getters.getUserId,
          payload: {
            sessionNr: sid
          }
        })
        .then(response => {
          console.log("SUC: " + JSON.stringify(response, null, 2));
        })
        .then(() => {
          const panels = this.$store.getters.getPanels.filter(
            p => p.name === sid
          );
          if (panels.length > 0) {
            this.$store.commit("removePanel", panels[0].panelId);
          }
        })
        .then(() => this.$root.getSessions())
        .catch(err => console.error("Can not close session.\n" + err));
    }
  },

  computed: {
    showTable() {
      return this.tables.length > 0;
    },
    showViews() {
      return this.views.length > 0;
    },
    getSessions() {
      return this.$store.getters.getSessions;
    }
  },

  created() {
    this.unwatchTables = this.$store.watch(
      (state, getters) => getters.getTableList,
      newValue => {
        console.log(
          `Navbar: Updating available tables.'` + JSON.stringify(newValue)
        );
        this.tables = newValue;
      }
    );

    this.unwatchViews = this.$store.watch(
      (state, getters) => getters.getViewList,
      newValue => {
        console.log(
          `Navbar: Updating available views.'` + JSON.stringify(newValue)
        );
        this.views = newValue;
      }
    );
  },

  beforeDestroy() {
    this.unwatchTables();
    this.unwatchViews();
  }
};
</script>
