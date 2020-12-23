<template>
  <v-container fluid>
    <v-row>
      <v-col v-for="panel in panels" v-bind:key="panel.panelId">
        <Editor
          v-if="panel.type == 'Editor'"
          :is="panel.type"
          v-bind:panelId="panel.panelId"
          v-bind:title="panel.name"
          v-on:removePanel="removePanel"
        ></Editor>
        <Table
          v-if="panel.type == 'Table'"
          :is="panel.type"
          v-bind:panelId="panel.panelId"
          v-bind:title="panel.name"
          v-on:removePanel="removePanel"
        ></Table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import Editor from "@/components/Editor";
import Table from "@/components/Table";

export default {
  components: { Table, Editor },

  data: () => ({
    name: "home",
  }),

  methods: {
    removePanel: function(panelId) {
      console.log("removePanel " + panelId + this);
      this.$store.commit("removePanel", panelId);
    },
  },

  computed: {
    panels() {
      return this.$store.getters.getPanels;
    },
  },
};
</script>
