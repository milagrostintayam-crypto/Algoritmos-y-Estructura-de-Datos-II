import hospedaje.*;
import servicios.HotelService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Interfaz gráfica sencilla del sistema de hospedaje. */
public class frmPrin {
    private final HotelService hotel;
    private final Path archivo = Path.of("datos", "hospedaje.dat");
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // Paleta sencilla: morado grisáceo, lavanda clara y gris suave.
    private static final Color MORADO = new Color(112, 96, 128);
    private static final Color MORADO_OSCURO = new Color(78, 67, 91);
    private static final Color LAVANDA = new Color(235, 230, 240);
    private static final Color FONDO = new Color(246, 244, 248);
    private static final Color GRIS_BORDE = new Color(211, 205, 217);
    private final JPanel panel = new JPanel(new BorderLayout(10,10));
    private final JTextField dni=new JTextField(12), nombres=new JTextField(20), telefono=new JTextField(12), ingreso=new JTextField(16), salida=new JTextField(16), adelanto=new JTextField("0",10);
    private final JComboBox<String> tipo=new JComboBox<>(new String[]{"Baño compartido","Baño privado"});
    private final JComboBox<Integer> numeroHabitacion=new JComboBox<>(new Integer[]{101,102,103,104,105});
    private DefaultTableModel reservas, habitaciones, cocheras;
    private JTable tablaReservas, tablaHabitaciones, tablaCocheras;
    private final JComboBox<String> reservaVehiculo=new JComboBox<>();
    private final JTextField placa=new JTextField(9);
    private final JComboBox<Cochera.Tipo> tipoVehiculo=new JComboBox<>(Cochera.Tipo.values());

    public frmPrin() throws IOException {
        hotel=HotelService.cargar(archivo);
        LocalDateTime ahora=hotel.ahora(); ingreso.setText(ahora.format(fmt)); salida.setText(ahora.plusDays(1).format(fmt));
        construir(); aplicarEstilo(); actualizar();
    }
    public JPanel getPanel(){return panel;}
    public boolean guardar(){try{hotel.guardar(archivo);return true;}catch(IOException e){error("No se pudieron guardar los datos: "+e.getMessage());return false;}}

    private void construir(){
        panel.setBorder(new EmptyBorder(16,16,16,16)); panel.setBackground(FONDO);
        JLabel titulo=new JLabel("HOSPEDAJE  |  Sistema de recepción"); titulo.setFont(new Font("SansSerif",Font.BOLD,24)); titulo.setForeground(MORADO_OSCURO);
        JLabel sub=new JLabel("Registro de huéspedes, reservas, habitaciones y cochera");
        JPanel cabecera=new JPanel(new GridLayout(2,1,0,4)); cabecera.setOpaque(false); cabecera.add(titulo);cabecera.add(sub);panel.add(cabecera,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.addTab("Recepción",recepcion());tabs.addTab("Reservas",vistaReservas());tabs.addTab("Habitaciones",vistaHabitaciones());tabs.addTab("Cochera",vistaCochera());panel.add(tabs,BorderLayout.CENTER);
    }
    private JPanel recepcion(){
        JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(16,8,8,8));
        JPanel f=new JPanel(new GridBagLayout());f.setBorder(BorderFactory.createTitledBorder("Datos del huésped y estadía"));GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(7,8,7,8);c.fill=GridBagConstraints.HORIZONTAL;
        campo(f,c,0,"DNI (8 dígitos)",dni);campo(f,c,1,"Nombres completos",nombres);campo(f,c,2,"Teléfono",telefono);campo(f,c,3,"Ingreso (AAAA-MM-DD HH:mm)",ingreso);campo(f,c,4,"Salida (AAAA-MM-DD HH:mm)",salida);campo(f,c,5,"Tipo de habitación",tipo);campo(f,c,6,"Habitación para ingreso inmediato",numeroHabitacion);campo(f,c,7,"Adelanto (S/)",adelanto);
        JPanel bs=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton ingresoBtn=boton("Registrar ingreso hoy"), reservaBtn=boton("Crear reserva"), limpiar=new JButton("Limpiar");
        ingresoBtn.addActionListener(e->accion(()->{int n=(Integer)numeroHabitacion.getSelectedItem();boolean privado=tipo.getSelectedIndex()==1;if(privado!=(n>=104))throw new IllegalArgumentException("101–103 son compartidas y 104–105 privadas. Elige un tipo y habitación compatibles.");Reserva r=hotel.registrarIngreso(huesped(),n,fecha(ingreso),fecha(salida),HotelService.monto(adelanto.getText()));actualizar();info("Ingreso registrado: R"+r.getId()+", habitación "+n+".");}));
        reservaBtn.addActionListener(e->accion(()->{Reserva r=hotel.solicitar(huesped(),tipo.getSelectedIndex()==1,fecha(ingreso),fecha(salida),HotelService.monto(adelanto.getText()));actualizar();info("Reserva R"+r.getId()+" creada. Asígnala en la pestaña Reservas.");}));
        limpiar.addActionListener(e->{dni.setText("");nombres.setText("");telefono.setText("");adelanto.setText("0");});bs.add(ingresoBtn);bs.add(reservaBtn);bs.add(limpiar);c.gridx=0;c.gridy=8;c.gridwidth=2;c.weightx=1;f.add(bs,c);
        p.add(f,BorderLayout.NORTH);p.add(new JLabel("Tarifas por noche: compartida S/ 30 · privada S/ 40 · fines de semana + S/ 10. Formato de fecha: 2026-10-30 14:00."),BorderLayout.CENTER);return p;
    }
    private void campo(JPanel p,GridBagConstraints c,int fila,String texto,JComponent componente){c.gridy=fila;c.gridx=0;c.gridwidth=1;c.weightx=.4;p.add(new JLabel(texto),c);c.gridx=1;c.weightx=.6;p.add(componente,c);}
    private JPanel vistaReservas(){
        JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,8,8,8));reservas=modelo("ID","DNI","Huésped","Habitación","Ingreso","Salida","Total","Pagado","Saldo","Estado");tablaReservas=new JTable(reservas);tablaReservas.setAutoCreateRowSorter(true);p.add(new JScrollPane(tablaReservas),BorderLayout.CENTER);
        JPanel bs=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton asignar=boton("Asignar pendientes"), llegada=new JButton("Registrar llegada"), pagar=new JButton("Registrar pago"), checkout=new JButton("Registrar salida"), cancelar=new JButton("Cancelar reserva");
        asignar.addActionListener(e->accion(()->{int n=hotel.asignarPendientes();actualizar();info("Se asignaron "+n+" reserva(s).");}));
        llegada.addActionListener(e->accion(()->{hotel.llegada(idReserva());actualizar();info("Llegada registrada.");}));
        pagar.addActionListener(e->accion(()->{String s=JOptionPane.showInputDialog(panel,"Importe del pago en soles:");if(s==null)return;hotel.pagar(idReserva(),HotelService.monto(s));actualizar();info("Pago registrado.");}));
        checkout.addActionListener(e->accion(()->{hotel.salida(idReserva());actualizar();info("Salida registrada. La habitación requiere limpieza.");}));
        cancelar.addActionListener(e->accion(()->{int id=idReserva();if(JOptionPane.showConfirmDialog(panel,"¿Cancelar reserva R"+id+"?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){long adel=hotel.cancelar(id);actualizar();info("Reserva cancelada. Adelanto registrado: "+HotelService.soles(adel));}}));
        bs.add(asignar);bs.add(llegada);bs.add(pagar);bs.add(checkout);bs.add(cancelar);p.add(bs,BorderLayout.SOUTH);return p;
    }
    private JPanel vistaHabitaciones(){
        JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,8,8,8));habitaciones=modelo("Habitación","Tipo","Tarifa base","Estado","Limpia","Habilitada");tablaHabitaciones=new JTable(habitaciones);p.add(new JScrollPane(tablaHabitaciones),BorderLayout.CENTER);
        JPanel bs=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton limpia=new JButton("Marcar limpia"), habilitar=new JButton("Habilitar / bloquear");limpia.addActionListener(e->accion(()->{hotel.marcarLimpia(idHabitacion());actualizar();info("Habitación marcada como limpia.");}));habilitar.addActionListener(e->accion(()->{hotel.cambiarHabilitada(idHabitacion());actualizar();}));bs.add(limpia);bs.add(habilitar);p.add(bs,BorderLayout.SOUTH);return p;
    }
    private JPanel vistaCochera(){
        JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,8,8,8));JPanel f=new JPanel(new FlowLayout(FlowLayout.LEFT));f.setBorder(BorderFactory.createTitledBorder("Registrar vehículo de un huésped alojado"));f.add(new JLabel("Reserva:"));f.add(reservaVehiculo);f.add(new JLabel("Placa:"));f.add(placa);f.add(tipoVehiculo);JButton registrar=boton("Registrar vehículo"), retirar=new JButton("Retirar vehículo");f.add(registrar);f.add(retirar);
        cocheras=modelo("ID","Plaza","Reserva","Placa","Tipo","Importe","Estado");tablaCocheras=new JTable(cocheras);p.add(f,BorderLayout.NORTH);p.add(new JScrollPane(tablaCocheras),BorderLayout.CENTER);
        registrar.addActionListener(e->accion(()->{String s=(String)reservaVehiculo.getSelectedItem();if(s==null)throw new IllegalArgumentException("No hay huéspedes alojados.");int id=Integer.parseInt(s.substring(1,s.indexOf(" ")));Cochera c=hotel.estacionar(id,placa.getText(),(Cochera.Tipo)tipoVehiculo.getSelectedItem());placa.setText("");actualizar();info("Vehículo registrado en plaza "+c.getPlaza()+". Importe: "+HotelService.soles(c.getImporte()));}));
        retirar.addActionListener(e->accion(()->{int fsel=tablaCocheras.getSelectedRow();if(fsel<0)throw new IllegalArgumentException("Selecciona un vehículo.");int id=(Integer)cocheras.getValueAt(tablaCocheras.convertRowIndexToModel(fsel),0);hotel.retirarVehiculo(id);actualizar();info("Retiro registrado.");}));return p;
    }
    private DefaultTableModel modelo(String... cols){return new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};}
    private JButton boton(String s){
        JButton b=new JButton(s);
        // BasicButtonUI respeta los colores definidos y evita el texto blanco sobre fondo claro en Windows.
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setBackground(MORADO);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MORADO_OSCURO), new EmptyBorder(7,11,7,11)));
        return b;
    }

    /** Aplica un estilo suave y uniforme sin cambiar la estructura básica del formulario. */
    private void aplicarEstilo() {
        panel.setBackground(FONDO);
        estilizar(panel);
    }

    private void estilizar(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JTabbedPane tabs) {
                tabs.setBackground(LAVANDA);
                tabs.setForeground(MORADO_OSCURO);
                tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
            } else if (componente instanceof JTable tabla) {
                tabla.setBackground(Color.WHITE);
                tabla.setForeground(new Color(65, 61, 70));
                tabla.setGridColor(GRIS_BORDE);
                tabla.setRowHeight(25);
                tabla.setSelectionBackground(new Color(218, 207, 230));
                tabla.setSelectionForeground(MORADO_OSCURO);
                tabla.getTableHeader().setBackground(LAVANDA);
                tabla.getTableHeader().setForeground(MORADO_OSCURO);
                tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
            } else if (componente instanceof JTextField campo) {
                campo.setBackground(Color.WHITE);
                campo.setForeground(new Color(55, 52, 60));
                campo.setCaretColor(MORADO_OSCURO);
                campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GRIS_BORDE), new EmptyBorder(5, 6, 5, 6)));
            } else if (componente instanceof JComboBox<?> combo) {
                combo.setBackground(Color.WHITE);
                combo.setForeground(new Color(55, 52, 60));
            } else if (componente instanceof JButton boton) {
                boton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
                boton.setOpaque(true);
                boton.setContentAreaFilled(true);
                boton.setFocusPainted(false);
                boton.setBackground(LAVANDA);
                boton.setForeground(MORADO_OSCURO);
                boton.setFont(new Font("SansSerif", Font.BOLD, 12));
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GRIS_BORDE), new EmptyBorder(7,11,7,11)));
            } else if (componente instanceof JPanel || componente instanceof JScrollPane) {
                componente.setBackground(FONDO);
            }
            if (componente instanceof Container hijo) estilizar(hijo);
        }
    }
    private Huesped huesped(){return new Huesped(dni.getText(),nombres.getText(),telefono.getText());}
    private LocalDateTime fecha(JTextField t){try{return LocalDateTime.parse(t.getText().trim(),fmt);}catch(Exception e){throw new IllegalArgumentException("Fecha inválida. Usa AAAA-MM-DD HH:mm, por ejemplo 2026-10-30 14:00.");}}
    private int idReserva(){int f=tablaReservas.getSelectedRow();if(f<0)throw new IllegalArgumentException("Selecciona una reserva.");return (Integer)reservas.getValueAt(tablaReservas.convertRowIndexToModel(f),0);}
    private int idHabitacion(){int f=tablaHabitaciones.getSelectedRow();if(f<0)throw new IllegalArgumentException("Selecciona una habitación.");return (Integer)habitaciones.getValueAt(tablaHabitaciones.convertRowIndexToModel(f),0);}
    private void actualizar(){
        if(reservas!=null){reservas.setRowCount(0);for(Reserva r:hotel.getReservas())reservas.addRow(new Object[]{r.getId(),r.getHuesped().getDni(),r.getHuesped().getNombres(),r.getHabitacion()==0?"Sin asignar":r.getHabitacion(),r.getIngreso().format(fmt),r.getSalida().format(fmt),HotelService.soles(hotel.total(r.getId())),HotelService.soles(r.getPagado()),HotelService.soles(hotel.saldo(r.getId())),r.getEstado()});}
        if(habitaciones!=null){habitaciones.setRowCount(0);for(Habitacion h:hotel.getHabitaciones())habitaciones.addRow(new Object[]{h.getNumero(),h.getTipo(),"S/ "+h.getTarifaBase(),hotel.estado(h.getNumero()),h.isLimpia()?"Sí":"No",h.isHabilitada()?"Sí":"No"});}
        if(cocheras!=null){cocheras.setRowCount(0);for(Cochera c:hotel.getCocheras())cocheras.addRow(new Object[]{c.getId(),c.getPlaza(),"R"+c.getReservaId(),c.getPlaca(),c.getTipo(),HotelService.soles(c.getImporte()),c.isActivo()?"En cochera":"Retirado"});}
        if(reservaVehiculo!=null){reservaVehiculo.removeAllItems();for(Reserva r:hotel.getReservas())if(r.getEstado()==Reserva.Estado.ALOJADA)reservaVehiculo.addItem("R"+r.getId()+" - "+r.getHuesped().getNombres());}
    }
    private void accion(Runnable r){try{r.run();guardar();}catch(Exception e){error(e.getMessage()==null?"Revisa los datos ingresados.":e.getMessage());}}
    private void info(String s){JOptionPane.showMessageDialog(panel,s,"Hospedaje",JOptionPane.INFORMATION_MESSAGE);}
    private void error(String s){JOptionPane.showMessageDialog(panel,s,"No se pudo completar",JOptionPane.WARNING_MESSAGE);}
}
