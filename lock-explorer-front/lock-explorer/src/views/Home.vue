<template>
  <v-container fluid fill-hight>
    <v-row v-if="userIdPresent">
      <v-col v-for="panel in panels" v-bind:key="panel.panelId">
        <Editor
          v-if="panel.type === 'Editor'"
          v-bind:panelId="panel.panelId"
          v-bind:title="panel.name"
          v-on:removePanel="removePanel"
        ></Editor>
        <DbTable
          v-if="panel.type === 'Table'"
          type="Table"
          v-bind:panelId="panel.panelId"
          v-bind:tableObj="panel.table"
          v-on:removePanel="removePanel"
        ></DbTable>
        <DbView
          v-if="panel.type === 'View'"
          v-bind:panelId="panel.panelId"
          v-bind:title="panel.name"
          v-on:removePanel="removePanel"
        ></DbView>
      </v-col>
    </v-row>
    <v-row v-else align="center" justify="center">
      <v-col cols="1" align="center">
        <v-progress-circular
          size="100"
          width="8"
          indeterminate
          color="primary"
        ></v-progress-circular>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import Editor from "@/components/Editor";
import DbTable from "@/components/DbTable";
import DbView from "@/components/DbView";

export default {
  components: { DbTable, Editor, DbView },

  data: () => ({
    name: "home"
  }),

  methods: {
    removePanel: function(panelId) {
      console.log("removePanel " + panelId + this);
      this.$store.commit("removePanel", panelId);
    }
  },

  computed: {
    panels() {
      return this.$store.getters.getPanels;
    },
    userIdPresent() {
      const loaded = this.$store.getters.getUserId;
      console.log("loaded: " + JSON.stringify(loaded));
      return loaded !== "";
    }
  }
};
</script>
