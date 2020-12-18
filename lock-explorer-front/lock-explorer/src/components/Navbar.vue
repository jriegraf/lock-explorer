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

    <template>
      <div class="pa-2">
        <v-btn color="primary" block @click="addEditor">
          <v-icon class="mr-2">mdi-code-braces</v-icon>New Session
        </v-btn>
      </div>
    </template>

    <template>
      <div class="pa-2">
        <v-menu top :offset-x="offset">
          <template v-slot:activator="{ on, attrs }">
            <v-btn color="primary" block dark v-bind="attrs" v-on="on">
              <v-icon class="mr-2">mdi-file-code</v-icon>
              Load Scenario
            </v-btn>
          </template>

          <v-list dense>
            <v-list-item-group max="0" v-model="selectedItem" color="primary">
              <v-list-item v-for="(item, i) in scenario_items" :key="i">
                <v-list-item-content>
                  <v-list-item-title v-text="item.text"></v-list-item-title>
                </v-list-item-content>
              </v-list-item>
            </v-list-item-group>
          </v-list>
        </v-menu>
      </div>
    </template>

    <v-treeview dense open-all :items="items"></v-treeview>
  </v-navigation-drawer>
</template>

<script>
export default {
  data: () => ({
    selectedItem: "",
    scenario_items: [{ text: "Lost Update" }, { text: "Deadlock" }],
    offset: true,
    items: [
      {
        id: 1,
        name: "Tables",
        children: [
          { id: 2, name: "Calendar" },
          { id: 3, name: "Chrome" },
          { id: 4, name: "Webstorm" },
        ],
      },
      {
        id: 5,
        name: "Views",
        children: [
          { id: 8, name: "index" },
          { id: 9, name: "bootstrap" },
        ],
      },
    ],
  }),

  methods: {
    addEditor: function() {
      console.log("addEditor");
      let editor = {type: "Editor", sid: "12987" }
      this.$store.commit("addPanel", editor);
    },
  },
};
</script>
