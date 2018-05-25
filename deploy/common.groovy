String getGitCommit() {
    return sh(script: 'git rev-parse HEAD', returnStdout: true)?.trim()
}

@NonCPS
boolean isGitTag(String desc) {
    match = desc =~ /.+-[0-9]+-g[0-9A-Fa-f]{6,}$/
    result = !match
    match = null // prevent serialisation
    return result
}

// 更新本地tags
void updateLocalGitTags(){
    // 删除本地所有tag，避免本地存在的tag在远程仓库中删除后，本地tag在同步时不会被
    sh "git tag -l | xargs git tag -d"
    // 拉取远程所有tag
    sh "git fetch --tags --force"
}

// 获取两个tag之间的commit msg
String getMsgBetweenTwoTags(String tag1, String tag2, boolean isDesc){
    if(isDesc){
        comm = "git log --pretty=%cd\\ %s --date=short $tag2..$tag1"
    }else{
        comm = "git log --pretty=%cd\\ %s --date=short $tag2..$tag1 | sort"
    }
    sh(script: "$comm", returnStdout: true)?.trim()
}

// 获取本地tag中的tag，时间排序
String getLatestTagName(){
    return getLatestTagName("1")[0]
    // comm = 'git for-each-ref "refs/tags" --format="%(refname:short)" --sort="-taggerdate" --count=1 '
    // return sh(script: "$comm", returnStdout: true)?.trim()
}

// 获取本地tag中的tag，时间排序
String[] getLatestTagName(String count){
    // comm = 'git for-each-ref "refs/tags" --format="%(refname:short)" --sort="-taggerdate" --count='+"$count"
    comm = 'git for-each-ref "refs/tags" --format="%(refname:short)" --sort="-creatordate" --count='+"$count"
    out = sh(script: "$comm", returnStdout: true)?.trim()
    rs = []
    out.split('\n').each{rs << it?.trim()}
    return rs
    
}

String getGitTagName() {
    commit = getGitCommit()
    if (commit) {
        desc = sh(script: "git describe --tags ${commit}", returnStdout: true)?.trim()
        if (isGitTag(desc)) {
            return desc
        }
    }
    return "manualbuild-${BUILD_ID}"
}

String getGitVersion() {
    return sh (returnStdout: true,
        script: 'git log --pretty=format:"GIT_VER=%h" -n 1 |awk -F GIT_VER= \'{print $2}\'').trim()    
}

void gitCheckout(String brancheName, String sshKeyId, String gitUrl){
    checkout([$class: 'GitSCM', branches: [[name: 'origin/'+brancheName]], 
        doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], 
        userRemoteConfigs: [[credentialsId: sshKeyId, url: gitUrl]]])
}

void gitCheckoutByTag(String tagName, String sshKeyId, String gitUrl){
    checkout([$class: 'GitSCM', branches: [[name: 'refs/tags/'+tagName]], 
        doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], 
        userRemoteConfigs: [[credentialsId: sshKeyId, url: gitUrl]]])
}

void update_BuildId(String file){
    sh "sed -i 's/BuildId *= *\".*\"/BuildId = \""+"${BUILD_ID}"+"\"/g' $file"
}

void update_GitVer(String file){
    def verinfo = getGitVersion()
    sh "sed -i 's/GitVer *= *\".*\"/GitVer = \""+"${verinfo}"+"\"/g' $file"
}

void update_GitTag(String file){
    def gittag = getGitTagName()
    sh "sed -i 's/GitTag *= *\".*\"/GitTag = \""+"${gittag}"+"\"/g' $file"
}

void win_RestoreNuget(String slnFile){
    bat "chcp 65001\r\n$NUGET restore $slnFile"
}

void win_RestoreNuget(String slnFile, String source){
    bat "chcp 65001\r\n$NUGET restore $slnFile -Source $source -Source https://api.nuget.org/v3/index.json"
}

void win_PackNuget(String nuspec){
    bat "chcp 65001\r\n$NUGET pack $nuspec"
}

void win_PackNuget(String nuspecFile, String outputDir){
    bat "chcp 65001\r\n$NUGET pack $nuspecFile -OutputDirectory $outputDir"
}

void win_PushNuget(String nupkgFile, String apikey, String source){
    bat "chcp 65001\r\n$NUGET push $nupkgFile -Apikey $apikey -Source $source"
}

void win_MsBuild35(String slnFile){
    bat "chcp 65001\r\n$MSBUILD35 $slnFile /t:Clean /p:Configuration=Release /clp:ErrorsOnly;Encoding=UTF-8;"
    bat "chcp 65001\r\n$MSBUILD35 $slnFile /t:Rebuild /p:Configuration=Release /clp:ErrorsOnly;Encoding=UTF-8;"    
}

void win_MsBuild40(String slnFile){
    bat "chcp 65001\r\n$MSBUILD40 $slnFile /t:Clean /p:Configuration=Release /clp:ErrorsOnly;Encoding=UTF-8;"
    bat "chcp 65001\r\n$MSBUILD40 $slnFile /t:Rebuild /p:Configuration=Release /clp:ErrorsOnly;Encoding=UTF-8;"    
}

void win_MakeInstaller(String nsisFile){
    bat "chcp 65001\r\n$MAKENSIS $nsisFile"
}

void win_Python34(String commd){
    bat "chcp 65501\r\n$PYTHON34 $commd"
}

void sendEmail(String to, String subject, String body, String attachments){
    emailext(
        to: "$to",
        subject: "$subject", 
        body: "$body",
        attachmentsPattern: "$attachments"
    )
}

void sendEmail(String to, String subject, String body){
    emailext(
        to: "$to",
        subject: "$subject", 
        body: "$body"
    )
}

return this
