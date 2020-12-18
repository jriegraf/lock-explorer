<template>
  <v-card dark>
    <v-card-title>
      <span class="mr-2">{{ title }}</span>
      <v-icon color="red">mdi-lock</v-icon>
      <v-spacer />
      <v-btn icon @click="$emit('removePanel', panelId)">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>

    <v-data-table
      dense
      dark
      hide-default-footer
      :headers="getTableData.headers"
      :items="getTableData.data"
      item-key="name"
      class="elevation-1"
    />
  </v-card>
</template>

<script>
export default {
  props: {
    title: { type: String, required: true },
    panelId: { type: Number, required: true },
  },
  data: () => ({}),
  computed: {
    getTableData() {
      try {
        return this.$store.getters.getTableDataA(this.title);
      } catch (err) {
        console.err(err);
        return [];
      }
    },
  },

  methods: {},
};
</script>
