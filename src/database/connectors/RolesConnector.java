package database.connectors;

import database.Connector;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RolesConnector extends Connector {

    private String role;
    private ResultSet rs;

    /**
     * Initializes table to "roles".
     */
    public RolesConnector() {
        super("roles");
    }

    /**
     * Adds a user application for a role to the database.
     *
     * @param roleName name of the role being applied for
     * @param userId ID number of the user making an application
     * @throws SQLException may be thrown when connecting to the database
     * or handling ResultSets
     */
    public void applyForRole(String roleName, long userId) throws SQLException {
        if (roleExists(roleName))
            addExistingRoleApplication(roleName, userId);
        else
            addNewRoleApplication(roleName, userId);
    }

    /**
     * Checks if a role exists in the database. Roles
     * exist in the database when they have been requested
     * by at least one user.
     *
     * @param roleName the name of the role being searched for
     * @return true if the role is found in a table
     * @throws SQLException may be thrown when making a prepared statement
     */
    private boolean roleExists(String roleName) throws SQLException {
        if (!role.equals(roleName)) setRole(roleName);
        boolean exists = false;
        if (rs.next()) exists = true;
        rs.beforeFirst();
        return exists;
    }

    /**
     * Adds a new role application to the database with an applicants userId
     * and the role they applied for. This can only be done if no other application
     * to the same role already exists.
     *
     * @param roleName the name of the role being applied for
     * @param userId the ID of the user applying for the role
     * @throws SQLException may be thrown when making a prepared statement
     * or when checking if the role exists
     */
    private void addNewRoleApplication(String roleName, long userId) throws SQLException {
        if (!roleExists(roleName)) {
            getConnection().prepareStatement("INSERT INTO roles VALUES ('"
                    + roleName + "', " + userId
                    + ", null, null)").executeUpdate();
        }
    }

    /**
     * Checks if a user already applied for a specified role.
     *
     * @param roleName the name of the role the user may have applied for
     * @param userId the ID number of the user who may have made a role application
     * @return true if the user already made a role application, false if they didn't
     * @throws SQLException may be thrown when checking if the role exists or querying a ResultSet
     */
    public boolean userAppliedForRole(String roleName, long userId) throws SQLException {
        if (!roleExists(roleName)) return false;
        return rs.getFloat("user1") == userId
                || rs.getFloat("user2") == userId
                || rs.getFloat("user3") == userId;
    }

    /**
     * Adds a user to an existing role application list. Does not work
     * on non-existent roles or duplicate applications.
     *
     * @param roleName the name of the role being applied for
     * @param userId the ID number of the user making an application
     * @throws SQLException may be thrown when checking if the role exists
     * or making a prepared statement
     */
    private void addExistingRoleApplication(String roleName, long userId) throws SQLException {
        if (!roleExists(roleName)) return;
        if (userAppliedForRole(roleName, userId)) return;

        int numApplicants = getNumApplications(roleName);
        if (numApplicants > 0 && numApplicants < 3) {
            getConnection().prepareStatement("UPDATE roles"
                    + " SET user" + (numApplicants + 1) + " = " + userId
                    + " WHERE name = '" + roleName + "'").executeUpdate();
        }
    }

    /**
     * Returns the number of people who applied for a specific role.
     *
     * @param roleName the name of the role being applied for
     * @return the number of people who applied for it (max of 3)
     * @throws SQLException may be thrown when checking if the role exists
     * or when querying a ResultSet
     */
    public int getNumApplications(String roleName) throws SQLException {
        if (!roleExists(roleName)) return 0;
        for (int i = 1; i <= 3; i++) {
            if (rs.getLong("user" + i) == 0) {
                return i - 1;
            }
        }
        return 4;
    }

    /**
     * Sets the instance variables to the role being manipulated/queried.
     *
     * @param roleName name of the role
     * @throws SQLException may be thrown when making a prepared statement
     */
    private void setRole(String roleName) throws SQLException {
        rs = getConnection().prepareStatement("SELECT * FROM roles "
                + "WHERE name = '" + roleName + "'").executeQuery();
        role = roleName;
    }

    /**
     * Has no functionality in this connector.
     *
     * @param userId ignored
     */
    @Override
    public void addUser(long userId) {
        System.out.println("Cannot add lone user to this table");
    }
}