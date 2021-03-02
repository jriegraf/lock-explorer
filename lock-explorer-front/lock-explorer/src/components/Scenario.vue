<template>
  <v-card tile min-width="35em" dark>
    <v-card-title
      ><span class="mr-4">Scenario: {{ title }}</span>
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
      <v-row dense v-for="item in content" :key="content.indexOf(item)">
        <v-col cols="12" class="pr-0">
          <v-row v-if="'comment' in item">
            <v-col cols="12">
              <span>{{ item.comment }}</span>
            </v-col>
          </v-row>

          <span v-if="'code' in item">
            <v-row
              v-for="statement in item.code"
              :key="item.code.indexOf(statement)"
            >
              <v-col cols="1" class="pr-0">
                <span class="session-number text-center"
                  >{{ statement.session }}:</span
                >
              </v-col>
              <v-col cols="10" class="px-0">
                <prism-editor
                  class="my-editor"
                  v-model="statement.sql"
                  :highlight="highlighter"
                />
              </v-col>
              <v-col cols="1" class="pl-1" align-self="center">
                <v-card-actions>
                  <v-tooltip open-delay="1000" bottom>
                    <template v-slot:activator="{ on, attrs }">
                      <v-btn
                        @click="runBtnClick(statement)"
                        icon
                        class="mx-1"
                        v-bind="attrs"
                        v-on="on"
                      >
                        <v-icon>mdi-play</v-icon>
                      </v-btn>
                    </template>
                    Execute this SQL statement.
                  </v-tooltip>
                </v-card-actions>
              </v-col>
            </v-row>
          </span>
        </v-col>
        <v-col v-if="content.indexOf(item) !== content.length - 1" cols="12">
          <v-divider></v-divider>
        </v-col>
      </v-row>
    </v-card-text>
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
    content: [
      {
        comment:
          "A deadlock is a situation in which two or more users are waiting for data locked by each other. Deadlocks prevent some transactions " +
          "from continuing to work.\nOracle Database automatically detects deadlocks and resolves them by rolling back one statement involved in the " +
          "deadlock, releasing one set of the conflicting row locks. The database returns a corresponding message to the transaction that undergoes " +
          "statement-level rollback. The statement rolled back belongs to the transaction that detects the deadlock. Usually, the signaled transaction " +
          "should be rolled back explicitly, but it can retry the rolled-back statement after waiting."
      },
      {
        comment:
          "Session 1 starts transaction 1 and updates the salary for employee 100. Session 2 starts transaction 2 and updates the salary for " +
          "employee 200. No problem exists because each transaction locks only the row that it attempts to update.",
        code: [
          {
            sql:
              "UPDATE employees SET salary = salary*1.1 WHERE employee_id = 100;",
            session: "S1"
          },
          {
            sql:
              "UPDATE employees SET salary = salary*1.1 WHERE employee_id = 200;",
            session: "S2"
          }
        ]
      },
      {
        comment:
          "Transaction 1 attempts to update the employee 200 row, which is currently locked by transaction 2. Transaction 2 attempts to " +
          "update the employee 100 row, which is currently locked by transaction 1.\n" +
          "A deadlock results because neither transaction can obtain the resource it needs to proceed or terminate. No matter how long each " +
          "transaction waits, the conflicting locks are held.",
        code: [
          {
            sql:
              "UPDATE employees SET salary = salary*1.1 WHERE employee_id = 200;",
            session: "S1"
          },
          {
            sql:
              "UPDATE employees SET salary = salary*1.1 WHERE employee_id = 100;",
            session: "S2"
          }
        ]
      },
      {
        comment:
          "Transaction 1 signals the deadlock and rolls back the UPDATE statement issued at t1. However, the update made at t0 is not " +
          "rolled back. The prompt is returned in session 1. Note: Only one session in the deadlock actually gets the deadlock error, but " +
          "either session could get the error."
      },
      {
        comment:
          "Session 1 commits the update made at t0, ending transaction 1. The update unsuccessfully attempted at t1 is not committed.",
        code: [
          {
            sql: "COMMIT;",
            session: "S1"
          }
        ]
      },
      {
        comment:
          "Session 2 commits the updates made at t0 and t1, which ends transaction 2.",
        code: [
          {
            sql: "COMMIT;",
            session: "S2"
          }
        ]
      }
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

    runSql(sql, sessionNr) {
      const body = {
        type: "EXECUTE_SQL",
        user: this.$store.getters.getUserId,
        payload: {
          sessionNr: sessionNr,
          sql: sql
        }
      };
      return this.$root.queryApi(body);
    },

    getSessionNr(sessionName) {
      return this.sessionsMap.filter(e => e.includes(sessionName)).flat()[1];
    },

    runBtnClick(statement) {
      this.loading = true;
      this.runSql(
        statement.sql.split(";").map(s => s.trim())[0],
        this.getSessionNr(statement.session)
      )
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
          console.log(this.successMessage);
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
    title: { type: String, required: true },
    panelId: { type: Number, required: true }
  },

  computed: {
    getSessions() {
      return this.$store.getters.getSessions;
    }
  },

  async beforeMount() {
    let uniqueSessions = this.content
      .filter(e => "code" in e)
      .flatMap(e => e.code)
      .map(e => e.session)
      .filter((v, i, a) => a.indexOf(v) === i);
    console.log("uniqueSessions: " + JSON.stringify(uniqueSessions));
    while (uniqueSessions.length > this.getSessions.length) {
      console.log("open sessions: " + this.getSessions.length);
      try {
        await this.$root.openSession();
      } catch (err) {
        console.error("Can not open session.\n" + err);
        break;
      }
    }
    this.sessionsMap = uniqueSessions.map((e, i) => [e, this.getSessions[i]]);
    console.log("sessionsMap: " + JSON.stringify(this.sessionsMap));
  }
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

.session-number {
  color: #ccc;
  font-family: Fira code, Fira Mono, Consolas, Menlo, Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
}

.prism-editor__textarea:focus {
  outline: none;
}
</style>
