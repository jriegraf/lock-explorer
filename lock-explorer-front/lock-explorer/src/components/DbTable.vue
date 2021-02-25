<template>
  <v-card dark min-width="35em">
    <v-card-title>
      <span class="mr-2">{{ tableObj.name }}</span>
      <v-chip v-if="tableObj.locked" class="ma-2">
        <v-icon class="mr-2" color="red">mdi-lock</v-icon>
        {{ tableObj.lock_type }}
      </v-chip>
      <v-chip v-else class="ma-2">
        <v-icon class="mx-1" color="red">mdi-lock-open</v-icon>
      </v-chip>
      <v-select
        v-if="type === 'Table'"
        v-model="session"
        label="Session"
        :items="this.$store.getters.getSessions"
      >
      </v-select>
      <v-spacer />
      <v-btn icon @click="$emit('removePanel', panelId)">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>

    <v-data-table
      :loading="loading"
      loading-text="Loading ..."
      load
      dense
      dark
      must-sort
      :headers="headers"
      :items="data"
      :item-class="tableRowClass"
      item-key="name"
      class="elevation-1"
    />

    <v-card-actions>
      <v-btn @click="fetchData">RELOAD</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  props: {
    tableObj: { type: Object, required: true },
    panelId: { type: Number, required: true },
    type: { type: String, required: true }
  },
  data: () => ({
    lockType: "RX",
    loading: true,
    headers: [],
    data: [],
    lockedRows: [],
    session: 0
  }),
  computed: {
  },

  methods: {
    tableRowClass: function(item) {
      return this.lockedRows.includes(item.ROWID) ? "lockedRow" : "";
    },
    fetchData: function() {
      this.loading = true;

      let body = null;
      if (this.type === "Table") {
        body = {
          type: "GET_TABLE",
          user: this.$root.$store.getters.getUserId,
          payload: {
            sessionNr: this.session,
            tableName: this.tableObj.name
          }
        };
      } else {
        body = {
          type: "GET_VIEW",
          user: this.$root.$store.getters.getUserId,
          payload: {
            sessionNr: 0,
            viewName: this.title
          }
        };
      }

      const selector = this.type === "Table" ? "tableData" : "viewData";

      this.$root
        .queryApi(body)
        .then(json => {
          console.info(
            "SUC: " + JSON.stringify(json.data.payload[selector], null, 2)
          );
          const payload = json.data.payload[selector];
          this.headers = payload.columnInfo
            .filter(col => col.columnName !== "ROWID")
            .map(v => ({
              text: v.columnName,
              value: v.columnName
            }));
          this.data = payload.data;
          if ("lockedRows" in payload) {
            this.lockedRows = payload.lockedRows;
          }
        })
        .catch(e => console.error("Error: " + JSON.stringify(e.response.data)))
        .finally(() => (this.loading = false));
    }
  },
  async mounted() {
    this.session = this.$root.$store.getters.getSessions[0];
    this.fetchData();
  }
};
</script>

<style>
.lockedRow {
  background-color: rgb(244, 67, 55);
}
</style>
