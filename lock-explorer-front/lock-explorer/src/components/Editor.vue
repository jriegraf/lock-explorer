<template>
  <v-card min-width="300px" dark>
    <v-card-title
      ><span class="mr-4">SID: {{ title }}</span>
      <v-spacer />
      <v-btn icon @click="$emit('removePanel', panelId)">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>
    <prism-editor
      class="my-editor"
      v-model="code"
      :highlight="highlighter"
      line-numbers
    />
    <v-card-actions>
      <v-expand-transition>
        <v-btn
          trans
          small
          color="secondary"
          v-if="state == states.editing"
          @click="runBtnClick"
        >
          Run<v-icon class="mx-2">mdi-step-forward</v-icon>
        </v-btn>
        <v-progress-circular
          v-if="state == states.loading"
          size="20"
          indeterminate
          color="primary"
          class="mx-4"
        ></v-progress-circular>
      </v-expand-transition>
    </v-card-actions>
  </v-card>
</template>

<script>
// import Prism Editor
import { PrismEditor } from "vue-prism-editor";
import "vue-prism-editor/dist/prismeditor.min.css"; // import the styles somewhere

// import highlighting library (you can use any library you want just return html string)
import { highlight, languages } from "prismjs/components/prism-core";
import "prismjs/components/prism-clike";
import "prismjs/components/prism-sql";
import "prismjs/themes/prism-tomorrow.css"; // import syntax highlighting styles

export default {
  components: {
    PrismEditor,
  },

  data: () => ({
    code: "SELECT * FROM table;\n",
    states: {
      editing: 0,
      loading: 1,
    },
    state: 0,
  }),

  methods: {
    highlighter(code) {
      return highlight(code, languages.sql); // languages.<insert language> to return html with markup
    },
    runBtnClick: function() {
      console.log("run pressed");
      this.state = this.states.loading;
      let loadingTime = Math.floor(Math.random() * Math.floor(5));
      console.log("loading for " + loadingTime + "sec");
      setTimeout(() => (this.state = this.states.editing), loadingTime * 1000);
    },
  },

  props: {
    title: { type: String, required: true },
    panelId: { type: Number, required: true },
  },
};
</script>

<style>
.my-editor {
  background: #2d2d2d;
  color: #ccc;
  font-family: Fira code, Fira Mono, Consolas, Menlo, Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
  padding: 5px;
}
.prism-editor__textarea:focus {
  outline: none;
}
</style>
