<template>
  <v-card dark min-width="35em">
    <v-card-title>
      <span class="mr-2">{{ title }}</span>
      <v-chip v-if="locked" class="ma-2">
        <v-icon class="mr-2" color="red">mdi-lock</v-icon>
        {{ lockType }}
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
    panelId: { type: Number, required: true },
    type: { type: String, required: true }
  },
  data: () => ({
    locked: true,
    lockType: "RX",
    loading: true,
    headers: [],
    data: [],
    session: 0
  }),
  computed: {
    getTableData() {
      try {
        return this.$store.getters.getTableData(this.title);
      } catch (err) {
        console.err(err);
        return [];
      }
    }
  },

  methods: {
    fetchData: function() {
      this.loading = true;

      let body = null;
      if (this.type === "Table") {
        body = {
          type: "GET_TABLE",
          user: this.$root.$store.getters.getUserId,
          payload: {
            sessionNr: this.session,
            tableName: this.title
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
          this.headers = json.data.payload[selector].columnInfo.map(v => ({
            text: v.columnName,
            value: v.columnName
          }));
          this.data = json.data.payload[selector].data;
        })
        .catch(e => console.error("Error: " + JSON.stringify(e.response.data)))
        .finally(() => (this.loading = false));
    }
  },
  async mounted() {
    this.fetchData();
  }
};
</script>
