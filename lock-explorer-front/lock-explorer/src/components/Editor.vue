<template>
  <v-card min-width="300px" dark>
    <v-card-title
      ><span class="mr-4">{{ editorTitle }}</span>

      <v-expand-transition>
        <v-btn
          trans
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
      <v-spacer />
      <v-alert
        class="ma-4"
        v-model="error"
        dense
        dismissible
        type="error"
        transition="scale-transition"
        >{{ errorMessage }}
      </v-alert>
      <v-alert
        v-if="showSuccessAlert"
        class="ma-4"
        dense
        type="success"
        transition="scale-transition"
        >Success
      </v-alert>
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
    timeout: 100000,
    snackbar: false,
    showSuccessAlert: false,
    errorMessage: "",
    error: false,
    sid: "",
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

    runSql(sql) {
      var myHeaders = new Headers();
      myHeaders.append("Accept", "application/json");
      myHeaders.append("Content-Type", "application/json");

      if (sql.endsWith(";")) {
        sql = sql.slice(0, sql.length - 1);
      }

      var raw = JSON.stringify({
        sql: sql,
      });

      var requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
      };

      return fetch("http://localhost:8080/sql/", requestOptions);
    },

    runBtnClick: async function() {
      this.state = this.states.loading;
      let statements = this.code.split(";").map((s) => s.trim());
      console.log("Statements: " + statements);
      let response = await this.runSql(statements[0]).catch((error) => {
        console.error(error);
      });

      let json = await response.json();
      if (response.status != 200) {
        this.errorMessage = `${json.status} ${json.message}`;
        this.error = true;
      } else {
        this.showSuccessAlert = true;
        setTimeout(() => (this.showSuccessAlert = false), 3 * 1000);
      }

      this.state = this.states.editing;
    },
  },

  computed: {
    editorTitle() {
      return "SID: " + this.sid;
    },
  },

  props: {
    title: { type: String, required: true },
    panelId: { type: Number, required: true },
  },

  async mounted() {
    var myHeaders = new Headers();
    myHeaders.append("Accept", "application/json");
    myHeaders.append("Content-Type", "application/json");

    var raw = JSON.stringify({
      sql:
        "SELECT SID FROM V$SESSION WHERE AUDSID = Sys_Context('USERENV', 'SESSIONID')",
    });

    var requestOptions = {
      method: "POST",
      headers: myHeaders,
      body: raw,
      redirect: "follow",
    };

    fetch("http://localhost:8080/sql/", requestOptions)
      .then((response) => {
        if (response.status == 200) {
          response.json().then((json) => {
            console.log("sid: " + json.data[0].SID);
            this.sid = json.data[0].SID;
          });
        } else {
          console.error("fetching sid failed");
          this.sid = "-";
        }
      })
        .catch((error) => console.log("error", error));
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
