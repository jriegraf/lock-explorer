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
        <v-btn color="primary" small block @click="addEditor">
          <v-icon class="mr-2">mdi-code-braces</v-icon>
          New Session
        </v-btn>
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
              <v-list-item v-for="(item, i) in scenario_items" :key="i">
                <v-list-item-icon>
                  <v-icon>mdi-menu-right</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <span v-text="item.text"></span>
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
            v-for="title in tables"
          :key="title"
          @click="openTable(title)"
        >
          <v-icon>mdi-menu-right</v-icon>
          <span class="text-lowercase" v-text="title"></span>
        </v-list-item>
      </v-list-group>

      <v-list-item v-else>
        <v-list-item-icon><v-icon>mdi-table</v-icon></v-list-item-icon>
        <v-list-item-title>
          No Tables available
        </v-list-item-title>
      </v-list-item>

      <v-list-group :value="showViews" prepend-icon="mdi-table">
        <template v-slot:activator>
          <v-list-item-title>Views</v-list-item-title>
        </template>
        <v-list-item
          small
          v-for="title in views"
          :key="title"
          @click="openTable(title)"
        >
          <v-icon>mdi-menu-right</v-icon>
          <v-list-item-title v-text="title"></v-list-item-title>
        </v-list-item>
      </v-list-group>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
export default {
  data: () => ({
    selectedItem: "",
    scenario_items: [{ text: "Lost Update" }, { text: "Deadlock" }],
    offset: true,
    tables: [],
    views: ["v$locked_object", "v$lock", "v$session", "v$transaction"]
  }),

  methods: {
    addEditor: function() {
      console.log("addEditor");
      let editor = { type: "Editor", name: "12987" };
      this.$store.commit("addPanel", editor);
    },
    openTable: function(tableName) {
      let table = { type: "Table", name: tableName };
      console.log("openTable " + JSON.stringify(table));
      this.$store.commit("addPanel", table);
    }
  },

  computed: {
    showTable() {
      return this.tables.length > 0;
    },
    showViews() {
      return this.views.length > 0;
    }
  },

  created() {
    this.unwatch = this.$store.watch(
      (state, getters) => getters.getTableList,
      newValue => {
        console.log(
          `Navbar: Updating available tables.'` + JSON.stringify(newValue)
        );
        this.tables = newValue;
      }
    );
  },

  beforeDestroy() {
    this.unwatch();
  }
};
</script>
