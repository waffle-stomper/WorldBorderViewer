import subprocess, os

rootdir = os.getcwd()


print('building...')
subprocess.call(['python','build.py'])

print('copying most recent jar to root...')
os.chdir('forge/build/libs')
files = sorted(os.listdir(os.curdir), reverse=True)
targetfile = files[0]
subprocess.call(['copy',targetfile,rootdir], shell=True)

print('Updating version numbers...')
os.chdir(rootdir)
subprocess.call(['python', 'Version Increment.py'], shell=False)

print('Copying jar to game dir...')
os.chdir(rootdir)
subprocess.call(['python', 'Copy To Minecraft Dir.py'], shell=False)


print('DONE! Press any key to exit...')
subprocess.check_output('pause', shell=True)

