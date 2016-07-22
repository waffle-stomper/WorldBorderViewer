import subprocess, os

rootdir = os.getcwd()

print('==========      Updating dependencies...      ===========')
os.chdir('forge')
subprocess.call(['gradlew.bat','--refresh-dependencies'])

print('==========      SetupDecompWorkspace...      ===========')
subprocess.call(['gradlew.bat','setupDecompWorkspace', 'eclipse'])

print('DONE! Press any key to exit...')
subprocess.check_output('pause', shell=True)
