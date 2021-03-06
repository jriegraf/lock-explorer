<template>
  <v-card tile min-width="35em" dark>
    <v-card-title
      ><span class="mr-4">SID: {{ title }}</span>
      <v-progress-circular
        v-if="loading"
        size="20"
        indeterminate
        color="primary"
        class="mx-4"
      ></v-progress-circular>

      <v-spacer />
      <v-btn icon @click="$emit('removePanel', panelId)">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>

    <v-card-text>
      <v-row
        v-for="content in editorContent"
        :key="editorContent.indexOf(content)"
      >
        <v-col cols="10" class="pr-0">
          <prism-editor
            class="my-editor"
            v-model="content.code"
            :highlight="highlighter"
          />
        </v-col>
        <v-col cols="1" class="pl-1" align-self="center">
          <v-card-actions>
            <v-tooltip open-delay="1000" bottom>
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  @click="runBtnClick(editorContent.indexOf(content))"
                  icon
                  :disabled="
                    !(content.code.includes(';') && content.code.length > 3)
                  "
                  class="mx-1"
                  v-bind="attrs"
                  v-on="on"
                >
                  <v-icon>mdi-play</v-icon>
                </v-btn>
              </template>
              Execute this SQL statement.
            </v-tooltip>
            <v-tooltip open-delay="1000" bottom>
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  v-bind="attrs"
                  v-on="on"
                  icon
                  @click="
                    editorContent.splice(editorContent.indexOf(content), 1)
                  "
                >
                  <v-icon dense small color="grey"
                    >mdi-trash-can-outline</v-icon
                  >
                </v-btn>
              </template>
              <span>Remove this statement from the editor window.</span>
            </v-tooltip>
          </v-card-actions>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <v-btn @click="addEditor">
            <v-icon class="mx-1">mdi-plus</v-icon>
            Add Statement
          </v-btn>
        </v-col>
      </v-row>
    </v-card-text>

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
        >{{ successMessage }}
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
    PrismEditor
  },

  data: () => ({
    editorContent: [
      { code: "SELECT user FROM dual;" },
      {
        code:
          "CREATE TABLE persons(\n" +
          "    person_id NUMBER GENERATED BY DEFAULT AS IDENTITY,\n" +
          "    first_name VARCHAR2(50) NOT NULL,\n" +
          "    last_name VARCHAR2(50) NOT NULL,\n" +
          "    PRIMARY KEY(person_id)\n" +
          ");"
      },
      {
        code:
          "INSERT INTO persons (first_name, last_name) VALUES ('John', 'Doe');"
      },
      {
        code: "UPDATE persons SET first_name = 'Johnny' WHERE person_id = 1;"
      },
      { code: "COMMIT;" }
    ],
    timeout: 100000,
    snackbar: false,
    showSuccessAlert: false,
    successMessage: "SUCCESS",
    errorMessage: "",
    error: false,
    loading: false
  }),

  methods: {
    highlighter(code) {
      return highlight(code, languages.sql);
    },

    runSql(sql) {
      const body = {
        type: "EXECUTE_SQL",
        user: this.$store.getters.getUserId,
        payload: {
          sessionNr: this.title,
          sql: sql
        }
      };
      return this.$root.queryApi(body);
    },

    addEditor() {
      this.editorContent.push({ code: "" });
    },

    runBtnClick(idx) {
      this.loading = true;
      let statements = this.editorContent[idx].code
        .split(";")
        .map(s => s.trim());
      console.log("Statements: " + statements);
      this.runSql(statements[0])
        .then(response => {
          let resp = response.data.payload.result;
          console.log("SUC: " + JSON.stringify(response, null, 2));
          this.error = false;
          if (resp["data"] !== undefined) {
            console.log(resp.data.length);
            if (resp.data.length === 1 && resp.columnInfo.length === 1) {
              let obj = resp.data[0];
              const [[key, value]] = Object.entries(obj);
              this.successMessage = key + ": " + value;
            } else {
              this.successMessage = `${resp.data.length} Rows affected.`;
            }
          } else {
            this.successMessage = `${resp.updated} Rows affected.`;
          }
          this.showSuccessAlert = true;
          setTimeout(() => (this.showSuccessAlert = false), 5 * 1000);
          return resp;
        })
        .then(() => this.$root.getAvailableTables())
        .catch(err => {
          let json = err.response.data;
          console.log("ERR: " + JSON.stringify(err.response.data, null, 2));
          this.showSuccessAlert = false;
          this.errorMessage = `${json.status} ${json.payload.Message}`;
          this.error = true;
        })
        .finally(() => {
          this.loading = false;
        });
    }
  },

  props: {
    title: { type: Number, required: true },
    panelId: { type: Number, required: true }
  },

  async beforeMount() {}
};
</script>

<style>
.my-editor {
  background: #2d2d2d;
  color: #ccc;
  font-family: Fira code, Fira Mono, Consolas, Menlo, Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
  padding: 1em;
}

.prism-editor__textarea:focus {
  outline: none;
}
</style>
