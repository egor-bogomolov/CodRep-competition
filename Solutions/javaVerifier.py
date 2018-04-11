import os
import glob
import sys


path_to_verifier = "/home/egor/Work/codrep/CodRep-competition/Java/build/libs/CodeRepJava-application-1.0.jar"


def run_verifier(path_to_task, should_replace=False, line_number=None):
    if should_replace:
        result = os.popen("java -jar " + path_to_verifier + " " + path_to_task + " " + str(line_number)).read()
    else:
        result = os.popen("java -jar " + path_to_verifier + " " + path_to_task + " -1").read()
    if result.startswith("OK"):
        return "OK"
    elif result.startswith("FAIL"):
        return "FAIL"
    elif result.startswith("Exception"):
        return "Exception"
    else:
        return "Unknown"


def main(paths):
    for path_to_dataset in paths:
        if os.path.isdir(path_to_dataset):
            path_to_tasks = os.path.join(path_to_dataset, "Tasks/")
            for task in os.listdir(path_to_tasks):
                if task.endswith(".txt"):
                    path_to_task = os.path.abspath(os.path.join(path_to_tasks, task))
                    sol = open(path_to_task.replace('Tasks', 'Solutions'), 'r')
                    answer = int(sol.readline())
                    print(path_to_task, answer)
                    result = os.popen("java -jar " + path_to_verifier + " " + path_to_task + " " + str(answer)).read()
                    if result.startswith("OK"):
                        print("Everything is ok!")
                    else:
                        print("Failed :(")
                        break


if __name__ == "__main__":
    main(sys.argv[1].split(':'))
