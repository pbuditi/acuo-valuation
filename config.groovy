environments {
    dev {
        appId = 'valuation-app'
        env = 'test'
        dataDir = 'src/main/resources'
        key = System.getenv('acuo_security_key')
    }
    
    docker {
        appId = 'valuation-app'
        env = 'docker'
        dataDir = '/data'
        key = System.getenv('acuo_security_key')
    }
}