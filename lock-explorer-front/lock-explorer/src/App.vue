<template>
  <v-app>
    <v-main>
      <Navbar />
      <v-container fill-height fluid>
        <v-fade-transition mode="out-in">
          <router-view></router-view>
        </v-fade-transition>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import Navbar from "@/components/Navbar";
export default {
  name: "App",

  components: { Navbar },

  data: () => ({
    registered: false
  }),

  mounted() {},

  async beforeMount() {
    await this.$root
      .queryApi({ type: "REGISTER" })
      .then(res => this.$store.commit("setUserId", res.data.payload.userId))
      .catch(e => console.error("Error: " + JSON.stringify(e.data)));

    this.$root.getAvailableTables();
    this.$root.getAvailableViews();
    this.$root.getSessions();
  }
};
</script>
