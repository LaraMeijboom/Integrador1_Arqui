package org.example;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlFactoryDao extends FactoryDao {
    private static MySqlFactoryDao instance = null;

    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String uri = "jdbc:mysql://localhost:3306/demodao";
    public static Connection conn;

    private MySqlFactoryDao() {
    }

    //Singleton
    public static synchronized MySqlFactoryDao getInstance() {
        if (instance == null) {
            instance = new MySqlFactoryDao();
        }
        return instance;
    }

    //Crea la conexion con la base sql
    public static Connection createConnection() {
        if (conn != null) {
            return conn;
        }
        String driver = DRIVER;
        try {
            Class.forName(driver).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            conn = DriverManager.getConnection(uri, "root", "");
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void closeConnection() {
        if (conn != null){
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void dropTables() throws SQLException {
        String dropCliente = "DROP TABLE IF EXISTS Cliente";
        this.conn.prepareStatement(dropCliente).execute();
        this.conn.commit();

        String dropFactura = "DROP TABLE IF EXISTS Factura";
        this.conn.prepareStatement(dropFactura).execute();
        this.conn.commit();

        String dropFacturaProducto = "DROP TABLE IF EXISTS Factura_Producto";
        this.conn.prepareStatement(dropFacturaProducto).execute();
        this.conn.commit();

        String dropProducto = "DROP TABLE IF EXISTS Producto";
        this.conn.prepareStatement(dropProducto).execute();
        this.conn.commit();
    }

    public void createTables() throws SQLException {
        String tableCliente = "CREATE TABLE IF NOT EXISTS Cliente (" +
                "    idCliente int  NOT NULL," +
                "    nombre varchar(500)  NOT NULL," +
                "    emal varchar(150)  NOT NULL," +
                "    CONSTRAINT Cliente_pk PRIMARY KEY (idCliente)" +
                ")";
        this.conn.prepareStatement(tableCliente).execute();
        this.conn.commit();
        String tableFactura = "CREATE TABLE IF NOT EXISTS Factura (" +
                "    idFactura int  NOT NULL," +
                "    idCliente int  NOT NULL," +
                "    CONSTRAINT Factura_pk PRIMARY KEY (idFactura)" +
                ")";
        this.conn.prepareStatement(tableFactura).execute();
        this.conn.commit();
        String tableFacturaProducto = "CREATE TABLE IF NOT EXISTS Factura_Producto (" +
                "    idFactura int  NOT NULL," +
                "    idProducto int  NOT NULL," +
                "    cantidad int  NOT NULL," +
                "    CONSTRAINT Factura_Producto_pk PRIMARY KEY (idFactura,idProducto)\n" +
                ")";
        this.conn.prepareStatement(tableFacturaProducto).execute();
        this.conn.commit();
        String tableProducto = "CREATE TABLE IF NOT EXISTS Producto (" +
                "    idProducto int  NOT NULL," +
                "    nombre varchar(45)  NOT NULL," +
                "    valor float(10,2)  NOT NULL," +
                "    CONSTRAINT Producto_pk PRIMARY KEY (idProducto)" +
                ")";
        this.conn.prepareStatement(tableProducto).execute();
        this.conn.commit();
    }

    private Iterable<CSVRecord> getData(String archivo) throws IOException {
        String path = "src\\recursos\\" + archivo;
        Reader in = new FileReader(path);
        String[] header = {};  // Puedes configurar tu encabezado personalizado aquí si es necesario
        CSVParser csvParser = CSVFormat.EXCEL.withHeader(header).parse(in);

        Iterable<CSVRecord> records = csvParser.getRecords();
        return records;
    }

    public void populateDB() throws Exception {
        try {
            System.out.println("Populating DB...");
            for(CSVRecord row : getData("clientes.csv")) {
                if(row.size() >= 3) { // Verificar que hay al menos 4 campos en el CSVRecord
                    String idString = row.get(0);
                    String name = row.get(1);
                    String email = row.get(2);
                    if(!idString.isEmpty() && !name.isEmpty() && !email.isEmpty()) {
                        try {
                            int id = Integer.parseInt(idString);
                            Cliente cliente = new Cliente(id, name, email);
                            insertCliente(cliente, conn);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de dirección: " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("Clientes insertados");

            for (CSVRecord row : getData("Facturas.csv")) {
                if (row.size() >= 2) { // Verificar que hay al menos 4 campos en el CSVRecord
                    String idFactura = row.get(0);
                    String idCliente = row.get(1);

                    if (!idFactura.isEmpty() && !idCliente.isEmpty()) {
                        try {
                            int idF = Integer.parseInt(idFactura);
                            int idC = Integer.parseInt(idCliente);

                            Factura factura = new Factura(idF, idC);
                            insertFactura(factura, conn);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de persona: " + e.getMessage());
                        }
                    }
                }
            }

            System.out.println("Facturas insertadas");

            for (CSVRecord row : getData("facturas-productos.csv")) {
                if (row.size() >= 2) { // Verificar que hay al menos 4 campos en el CSVRecord
                    String idFactura = row.get(0);
                    String idProducto = row.get(1);
                    String cantidad = row.get(2);

                    if (!idFactura.isEmpty() && !idProducto.isEmpty() && !cantidad.isEmpty()) {
                        try {
                            int idF = Integer.parseInt(idFactura);
                            int idP = Integer.parseInt(idProducto);
                            int cant = Integer.parseInt(idProducto);

                            FacturaProducto facturaProducto = new FacturaProducto(idF, idP,cant);
                            insertFacturaProducto(facturaProducto, conn);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de persona: " + e.getMessage());
                        }
                    }
                }
            }

            System.out.println("Facturas-Productos insertadas");

            for (CSVRecord row : getData("productos.csv")) {
                if (row.size() >= 3) { // Verificar que hay al menos 4 campos en el CSVRecord
                    String idProducto = row.get(0);
                    String nombre = row.get(1);
                    String valor = row.get(2);

                    if (!idProducto.isEmpty() && !nombre.isEmpty() && !valor.isEmpty()) {
                        try {
                            int idP = Integer.parseInt(idProducto);
                            int val = Integer.parseInt(valor);

                            Producto Producto = new Producto(idP,nombre,val);
                            insertProducto(Producto, conn);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de persona: " + e.getMessage());
                        }
                    }
                }
            }

            System.out.println("Productos insertados");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //Insertar SQL de cliente
    private int insertCliente (Cliente cliente, Connection conn) throws Exception{
        String insert = "INSERT INTO Cliente (idCliente, nombre, email) VALUES (?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(insert);
            ps.setInt(1,cliente.getIdCliente());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getEmail());
            if (ps.executeUpdate() == 0) {
                throw new Exception("No se pudo insertar");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePsAndCommit(conn, ps);
        }
        return 0;
    }

    //Insertar SQL de cliente
    private int insertFactura (Factura factura, Connection conn) throws Exception{
        String insert = "INSERT INTO Factura (idFactura, idCliente) VALUES (?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(insert);
            ps.setInt(1,factura.getIdFactura());
            ps.setInt(2, factura.getIdCliente());
            if (ps.executeUpdate() == 0) {
                throw new Exception("No se pudo insertar");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePsAndCommit(conn, ps);
        }
        return 0;
    }

    //Insertar SQL de cliente
    private int insertProducto (Producto producto, Connection conn) throws Exception{
        String insert = "INSERT INTO Producto (idProducto, nombre, valor) VALUES (?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(insert);
            ps.setInt(1,producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setFloat(3, producto.getValor());
            if (ps.executeUpdate() == 0) {
                throw new Exception("No se pudo insertar");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePsAndCommit(conn, ps);
        }
        return 0;
    }

    //Insertar SQL de cliente
    private int insertFacturaProducto (FacturaProducto facturaProducto, Connection conn) throws Exception{
        String insert = "INSERT INTO Cliente (idFactura, idProducto, cantidad) VALUES (?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(insert);
            ps.setInt(1,facturaProducto.getIdFactura());
            ps.setInt(2, facturaProducto.getIdProducto());
            ps.setFloat(3, facturaProducto.getCantidad());
            if (ps.executeUpdate() == 0) {
                throw new Exception("No se pudo insertar");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePsAndCommit(conn, ps);
        }
        return 0;
    }

    @Override
    public ClienteDao getClienteDao() throws SQLException {
        return new ClienteJDBC_MySql(createConnection());
    }

    @Override
    public FacturaDao getFacturaDao() throws SQLException {
        return null;
    }

    private void closePsAndCommit(Connection conn, PreparedStatement ps) {
        if (conn != null){
            try {
                ps.close();
                conn.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}