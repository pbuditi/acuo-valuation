environments {
    test {
        appId = 'valuation-app'
        env = 'test'
        dataDir = 'src/main/resources'
        key = System.getenv('acuo_security_key')
    }

    dev {
        appId = 'valuation-app'
        env = 'dev'
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