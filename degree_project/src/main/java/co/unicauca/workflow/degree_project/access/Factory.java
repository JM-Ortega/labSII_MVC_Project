package co.unicauca.workflow.degree_project.access;

public class Factory {
    private static Factory instance;
    
    private Factory() {
    }
    
    public static Factory getInstance() {
        if (instance == null) {
            instance = new Factory();
        }
        return instance;
    }
    
    public IUserRepository getRepository(String type) {
        IUserRepository result = null;
        switch (type) {
            case "default":
                result = new SqliteRepository();
                break;
        }
        return result;
    }
}
