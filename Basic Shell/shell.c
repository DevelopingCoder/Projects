#include <ctype.h>
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <signal.h>
#include <sys/wait.h>
#include <termios.h>
#include <unistd.h>
#include "tokenizer.h"
#include <sys/stat.h>
#include <fcntl.h>

/* Whether the shell is connected to an actual terminal or not. */
bool shell_is_interactive;

/* File descriptor for the shell input */
int shell_terminal;

/* Terminal mode settings for the shell */
struct termios shell_tmodes;

/* Process group id for the shell */
pid_t shell_pgid;

//List of processes running in the background
int num_processes = 0;
int processes[1000];

int cmd_exit(struct tokens *tokens);
int cmd_help(struct tokens *tokens);
int cmd_pwd(struct tokens *tokens);
int cmd_cd(struct tokens* tokens);
int cmd_wait(struct tokens* tokens);
int cmd_fg(struct tokens *tokens);
int cmd_bg(struct tokens *tokens);

/* Built-in command functions take token array (see parse.h) and return int */
typedef int cmd_fun_t(struct tokens *tokens);

/* Built-in command struct and lookup table */
typedef struct fun_desc {
  cmd_fun_t *fun;
  char *cmd;
  char *doc;
} fun_desc_t;

fun_desc_t cmd_table[] = {
  {cmd_help, "?", "show this help menu"},
  {cmd_exit, "exit", "exit the command shell"},
  {cmd_pwd, "pwd", "display the working directory"},
  {cmd_cd, "cd", "change WD to another directory"},
  {cmd_wait, "wait", "waits for background process's to finish"},
  {cmd_fg, "fg", "moved a process to the fg"},
  {cmd_bg, "bg", "resume a background process"}
};

int cmd_fg(struct tokens *tokens) {
  int length = tokens_get_length(tokens);
  if (length != 2) //move most recent into fg
  {
    tcsetpgrp(0, processes[num_processes]);
    num_processes-=1;
  } else {
    int pid = tokens_get_token(tokens, 1);
    for (int i = 0; i < num_processes; ++i)
    {
      if (processes[num_processes] == pid)
      {
        tcsetpgrp(0, processes[num_processes]);
        processes[num_processes] = -1;
      }
    }
  }
  return 0;
}

int cmd_bg(struct tokens *tokens) {
  
}

int cmd_wait(struct tokens *tokens) {
  for (int i = 0; i < num_processes; ++i)
  {
    waitpid(processes[i], NULL, 0);
  }
  return 0;
}

/* Prints a helpful description for the given command */
int cmd_help(struct tokens *tokens) {
  for (int i = 0; i < sizeof(cmd_table) / sizeof(fun_desc_t); i++)
    printf("%s - %s\n", cmd_table[i].cmd, cmd_table[i].doc);
  return 1;
}

/* Exits this shell */
int cmd_exit(struct tokens *tokens) {
  exit(0);
}

int cmd_pwd(struct tokens* tokens) {
  char buffer[1024];
  getcwd(buffer, sizeof(buffer));
  printf("%s\n", buffer);
  return 1;
}

int cmd_cd(struct tokens* tokens) {
  if (chdir(tokens_get_token(tokens, 1)) == -1) {
    return -1;
  } return 0;
}

/* Looks up the built-in command, if it exists. */
int lookup(char cmd[]) {
  for (int i = 0; i < sizeof(cmd_table) / sizeof(fun_desc_t); i++)
    if (cmd && (strcmp(cmd_table[i].cmd, cmd) == 0))
      return i;
  return -1;
}

/* Intialization procedures for this shell */
void init_shell() {
  /* Our shell is connected to standard input. */
  shell_terminal = STDIN_FILENO;

  /* Check if we are running interactively */
  shell_is_interactive = isatty(shell_terminal);

  if (shell_is_interactive) {
    /* If the shell is not currently in the foreground, we must pause the shell until it becomes a
     * foreground process. We use SIGTTIN to pause the shell. When the shell gets moved to the
     * foreground, we'll receive a SIGCONT. */
    while (tcgetpgrp(shell_terminal) != (shell_pgid = getpgrp()))
      kill(-shell_pgid, SIGTTIN);

    /* Saves the shell's process id */
    shell_pgid = getpid();

    /* Take control of the terminal */
    tcsetpgrp(shell_terminal, shell_pgid);

    /* Save the current termios to a variable, so it can be restored later. */
    tcgetattr(shell_terminal, &shell_tmodes);
  }
}

int has_slash(char* path) {
  int length = strlen(path);
  for (int i = 0; i < length; ++i)
  {
    if (path[i] == '/')
    {
      return 0;
    }
  }
  return -1;
}


int run_program(struct tokens* tokens) {
  //Takes in a list of tokens that include the path and arguments for the program
  int token_length = tokens_get_length(tokens);
  
  if (token_length < 1)
  {
    printf("You must enter a valid command\n");
    return 0;
  }

  char* program = tokens_get_token(tokens, 0);
  //Grab arguments for program
  char* arguments[token_length + 1];
  int total_args = 0;
  for (int i = 0; i < token_length; ++i)
  {
    char* argument = tokens_get_token(tokens, i);

    //Assess redirection
    if (strcmp(argument, "<") == 0)
    {
      int fd = open(tokens_get_token(tokens, i+1), O_RDONLY);
      dup2(fd, 0);
      i+=1;
    } else if (strcmp(argument, ">") == 0)
    {
      int fd = open(tokens_get_token(tokens, i+1), O_CREAT|O_WRONLY, 0644);
      dup2(fd, 1);
      i+=1;
    } else if (strcmp(argument, "&") != 0) {
      //No redirection
      arguments[i] = argument;
      total_args += 1;
    }
  }
  arguments[total_args] = '\0'; //add NULL Terminator to the end
  //Check if we need to explore the PATH variable (no slashes in path)
  if (has_slash(program) < 0) 
  {
    char* path_env = getenv("PATH");
    char* token = strtok(path_env, ":");
    //modify program to have enough memory space
    char* full_path;
    while (token != NULL) {
      full_path = malloc((strlen(path_env) + strlen(program)) * sizeof(char));

      //copy token into full_path and strcat the program
      strcpy(full_path, token);
      sprintf(full_path, "%s/%s", full_path,program);
      //Fork a child and execute
      pid_t child_pid = fork();
      if (child_pid == 0)
      {
        execv(full_path, arguments);
        exit(2); //The exec command failed
      } else {
        int status;
        wait(&status);
        if (WEXITSTATUS(status) != 2)
        {
          return 0; //The program is a success!
        }
      }

      //free full path and grab the next environment path
      free(full_path);
      token = strtok(NULL, ":");
    }

    //Do one more path run from the current directory
    char current_path[1024];
    getcwd(current_path, sizeof(current_path));
    sprintf(current_path, "%s/%s", current_path,program);
    pid_t child_pid = fork();
    if (child_pid == 0)
    {
      execv(current_path, arguments);
      exit(2); //The exec command failed
    } else {
      free(full_path);
      int status;
      wait(&status);
      if (!WIFEXITED(status))
      {
        return 0; //The program is a success!
      }
    }
  } 
  else { //Otherwise, assume program is the full path
    execv(program, arguments);
  }
  return -1;
}

void sig_handler(int sig) {
  printf("received a signal. pid is %d\n", getpid());
}

int main(int argc, char *argv[]) {
  init_shell();

  static char line[4096];
  int line_num = 0;

  /* Please only print shell prompts when standard input is not a tty */
  if (shell_is_interactive)
    fprintf(stdout, "%d: ", line_num);
  int bg = 0;
  while (fgets(line, 4096, stdin)) {
    /* Split our line into words. */
    struct tokens *tokens = tokenize(line);

    /* Find which built-in function to run. */
    int fundex = lookup(tokens_get_token(tokens, 0));

    if (fundex >= 0) {
      cmd_table[fundex].fun(tokens);
    } else {
      /* REPLACE this to run commands as programs. */

      //Fork in order to account for redirecting
      //If in background, ignore sigttou
      signal(SIGTTOU, SIG_IGN);

      pid_t child_pid = fork();
      
      
      //check for background processing token
      
      if (strcmp("&", tokens_get_token(tokens, tokens_get_length(tokens) - 1)) == 0)
      {
        bg = 1;
      }

      //Now set the child to have a new pgid
      if (child_pid == 0)
      {
        setpgid(getpid(), getpid());
        if (bg == 0) //push it into the foreground
        {
          tcsetpgrp(0, getpid());
        }
        run_program(tokens);
        exit(2);
      }

      //If not background, wait for execution and set shell back to foreground
      if (bg == 0)
      {
        int temp;
        wait(&temp);
        tcsetpgrp(0, getpid());
      } else {
        processes[num_processes] = child_pid;
        num_processes += 1;
      }
    }
    bg = 0;

    if (shell_is_interactive)
      /* Please only print shell prompts when standard input is not a tty */
      fprintf(stdout, "%d: ", ++line_num);

    /* Clean up memory */
    tokens_destroy(tokens);
  }

  return 0;
}
