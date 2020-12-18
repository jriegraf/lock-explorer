<template>
  <v-app>
    <v-main>
      <Navbar />
      <v-container fluid>
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
    //
  }),
};
</script>
