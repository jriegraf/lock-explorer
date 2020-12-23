<template>
  <v-card dark>
    <v-card-title>
      <span class="mr-2">{{ title }}</span>
      <v-chip v-if="locked" class="ma-2">
        <v-icon class="mr-2" color="red">mdi-lock</v-icon>
        {{ lockType }}
      </v-chip>

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

      <v-btn>RELOAD</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  props: {
    title: { type: String, required: true },
    panelId: { type: Number, required: true },
  },
  data: () => ({
    locked: true,
    lockType: "RX",
    loading: true,
    headers: [],
    data: [],
  }),
  computed: {
    getTableData() {
      try {
        return this.$store.getters.getTableData(this.title);
      } catch (err) {
        console.err(err);
        return [];
      }
    },
  },

  methods: {},
  async mounted() {
    var myHeaders = new Headers();
    myHeaders.append("Accept", "application/json");
    myHeaders.append("Content-Type", "application/json");

    var raw = JSON.stringify({
      sql: "SELECT * FROM " + this.title,
    });

    var requestOptions = {
      method: "POST",
      headers: myHeaders,
      body: raw,
      redirect: "follow",
    };

    let json = await fetch("http://localhost:8080/sql/", requestOptions)
      .then((response) => response.json())
      .catch((error) => console.log("error", error))
      .finally((this.loading = false));

    if (json != undefined) {
      this.headers = json.columnInfo.map((v) => ({
        text: v.columnName,
        value: v.columnName,
      }));
      this.data = json.data;
    }
  },
};
</script>
