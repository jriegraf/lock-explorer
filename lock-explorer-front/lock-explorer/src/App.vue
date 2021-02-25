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
    let userId = null;
    while (userId == null) {
      userId = await this.$root
        .queryApi({ type: "REGISTER" })
        .then(res => {
          this.$store.commit("setUserId", res.data.payload.userId);
          return res.data.payload.userId;
        })
        .catch(e => {
          console.error("Error: " + JSON.stringify(e.data));
          return null;
        });
    }

    this.$root.getAvailableTables();
    this.$root.getAvailableViews();
    this.$root.getSessions().then(sessions => {
      console.log("Sessions: " + JSON.stringify(sessions));
      const editor = { type: "Editor", name: sessions[0] };
      this.$store.commit("addPanel", editor);
    });
  }
};
</script>
