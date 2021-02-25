<template>
  <v-card dark min-width="35em">
    <v-card-title>
      <span class="mr-2">{{ title }}</span>
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
    title: { type: String, required: true },
    panelId: { type: Number, required: true }
  },
  data: () => ({
    loading: true,
    headers: [],
    data: [],
    lockedRows: [],
    session: 0
  }),
  computed: {},

  methods: {
    tableRowClass: function(item) {
      return this.lockedRows.includes(item.ROWID) ? "lockedRow" : "";
    },
    fetchData: function() {
      this.loading = true;

      let body = {
        type: "GET_VIEW",
        user: this.$root.$store.getters.getUserId,
        payload: {
          sessionNr: this.session,
          viewName: this.title
        }
      };

      const selector = "viewData";

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

</style>
