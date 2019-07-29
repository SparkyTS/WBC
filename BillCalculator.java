package WaterBillCalculator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import static javax.swing.SwingConstants.CENTER;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

public class BillCalculator extends JPanel {
    private static final String CONNECTION = "jdbc:mysql://localhost:3306/db1?useSSL=false";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static Statement statement;

    private JTextField[] currentReading = new JTextField[10];
    private JLabel[] differenceLabels = new JLabel[10];
    private int[] lastReading = new int[10];
    private JLabel[] amounts = new JLabel[10];
    private JTextField total;
    private JButton calculate,reset;
    private int sumOfDifference = 0,amount;
    private JLabel totalDifference =new JLabel("Total Diff:",SwingConstants.CENTER);
    private JLabel totalAmount = new JLabel("",CENTER);

    //constructor
    public BillCalculator() {
        //Connecting to the database for fetching the previous data.
        try {
            Class.forName(DRIVER);
            Connection connection = DriverManager.getConnection(CONNECTION,"root","");
            statement = connection.createStatement();
            //statement.executeUpdate("CREATE TABLE Users (HouseNo INT NOT NULL PRIMARY KEY, Name Varchar(32))");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this,"Could not connect to the database(hint:Check WAMP Server)","Connectivity Error",JOptionPane.INFORMATION_MESSAGE);
            //e.printStackTrace();
        }

        setLayout(new GridLayout(10+1+1,2+1+1+1+1,10,10));
        showData();
        setVisible(true);
    }

    //displaying data on GUI and Fetching Previous data from database.
    private void showData() {
        add(new JLabel("House No",CENTER));add(new JLabel("Names",CENTER));
        add(new JLabel("Current Reading",CENTER));add(new JLabel("Previous Reading",CENTER));
        add(new JLabel("Current-Previous",CENTER));add(new JLabel("Amount",CENTER));
        try {
           ResultSet resultSet =  statement.executeQuery("Select * from Users Order By HouseNo ASC;");
           int i = 0;
           while (resultSet.next()){
               /**1*/add(new JLabel(String.valueOf(resultSet.getInt("HouseNo")),CENTER));
               /**2*/add(new JLabel(resultSet.getString("Name"),CENTER));

               currentReading[i] = new JTextField("");
               /**3*/add(currentReading[i]);

               /**4*/add(new JLabel(String.valueOf(lastReading[i]=resultSet.getInt("LastReading")),CENTER));

               /**5*/add(differenceLabels[i]=new JLabel("     ",CENTER));

               /**6*/add(amounts[i]=new JLabel("",CENTER));
//               System.out.printf("%-5d %-15s%n",resultSet.getInt("HouseNo"),resultSet.getString("Name"));
               i++;
           }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        add(new JLabel("Total Amount :"));add(total=new JTextField("",5));
        add(calculate = new JButton("Calculate"));
        add(reset = new JButton("Reset"));
        add(totalDifference);
        add(totalAmount);

        calculate.addActionListener(e -> verifyAndUpdate());
        reset.addActionListener(e -> resetCurrent());
    }

    //for clearing out the values in the current reading to null.
    private void resetCurrent() {
//        for(JTextField reading : currentReading){
//            reading.setText("");
//            reading.setEditable(true);
//        }
        for (int i = 0; i < 10; i++) {
            currentReading[i].setText("");
            currentReading[i].setEditable(true);
            differenceLabels[i].setText("");
            amounts[i].setText("");
        }
        totalDifference.setText("");
        total.setText("");
        totalAmount.setText("");
    }

    private void verifyAndUpdate() {
        try{
            amount = Integer.parseInt(total.getText());
            sumOfDifference = 0;
            if(amount<0) throw new NumberFormatException("Total Amount Can't be negative.");
            for(int i = 0,difference ; i < 10 ; i++) {
                difference = Integer.parseInt(currentReading[i].getText())-lastReading[i];
                if(difference<0) throw new NumberFormatException("Current Reading Can Not Be Lesser Then Previous");
                currentReading[i].setEditable(false);
                differenceLabels[i].setText(String.valueOf(difference));
                sumOfDifference+=difference;
            }
            totalDifference.setText("Total Diff : "+sumOfDifference);
            generateBill();
        }catch (NumberFormatException e){
            switch (e.getMessage()) {
                case "Current Reading Can Not Be Lesser Then Previous":
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Entry", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "Total Amount Can't be negative.":
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Entry", JOptionPane.INFORMATION_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Current reading or total amount can't be empty and can only have digits.", "Value Required", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        }
    }

    private void generateBill() {
        int sum = 0; float pr=amount*1.0f/sumOfDifference;
        for(int i = 0 ; i < 10 ; i++){

        float value = Float.parseFloat(differenceLabels[i].getText())*pr;
        if(value-Math.floor(value)<0.50){
            amounts[i].setText(String.valueOf((int)value));
            sum += (int)value;
        }
        else {
            amounts[i].setText(String.valueOf((int)(value+1)));
            sum += (int)(value + 1);
        }


        //amounts[i].setText(String.valueOf(rs=(int)Math.ceil(Float.parseFloat(differenceLabels[i].getText())*pr)));
        amounts[i].setForeground(Color.RED);
        //sum+=rs;
        }
        totalAmount.setText("Total : "+sum);
    }

    private void save() {
            final String currentValidation = "Current value is less then Previous";
        try{
            //verifying that only int data present in the currentReading Field.
            //the next block of statement throws the NumberFormatException when data can't be parsed to Integer.

//            if(Integer.parseInt(currentReading[0].getText())<lastReading[0]) throw new NumberFormatException(currentValidation);
//            if(Integer.parseInt(currentReading[1].getText())<lastReading[1]) throw new NumberFormatException(currentValidation);
//            for(int i = 2 ; i < 10 ; i++)
//            if(Integer.parseInt(currentReading[i].getText())<lastReading[i]) throw new NumberFormatException(currentValidation);
            //optimizing the value with only one check
            //optimizing the value with only one check
            if(currentReading[9].isEditable()){
                throw new NumberFormatException("Please Calculate The Value Again");
//                JOptionPane.showMessageDialog(null,"Please Calculate The Value Again","Not Calculated",JOptionPane.INFORMATION_MESSAGE);
            }


            statement.executeUpdate("Update Users SET LastReading = " + currentReading[0].getText() + " where HouseNO = 998");
            statement.executeUpdate("Update Users SET LastReading = " + currentReading[1].getText() + " where HouseNO = 999");
            for(int i = 1001 ; i<=1008 ; i++){
                statement.executeUpdate("Update Users SET LastReading = " + currentReading[i - 999].getText() + " where HouseNO = " + i);
            }
            System.exit(0);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e){
            if(e.getMessage().equals(currentValidation))
                JOptionPane.showMessageDialog(this,currentValidation,"Validation",JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(this,"Not a Valid Data in current Reading.","Validation",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
       BillCalculator billCalculator = new BillCalculator();
       JFrame application = new JFrame("Water Bill Calculator");
       JLabel dateAndTimeDisp = new JLabel(Calendar.getInstance().getTime().toString());
       application.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
       application.setLocationRelativeTo(null);
       application.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                int selectedOption = JOptionPane.showConfirmDialog(null,"Save The Current Reading as Previous?","Save", JOptionPane.YES_NO_CANCEL_OPTION);
                if(selectedOption==JOptionPane.YES_OPTION){
                    billCalculator.save();
                }
                else if(selectedOption==JOptionPane.NO_OPTION) System.exit(0);
            }
        });
       application.add(dateAndTimeDisp,BorderLayout.PAGE_START);
       application.add(billCalculator);
       application.add(new JLabel("Developed By Tanay Shah"),BorderLayout.PAGE_END);
       application.setSize(820,550);
       application.setLocationRelativeTo(null);
       application.setVisible(true);
       Thread timeThread = new Thread(() -> {
        while (true){
        dateAndTimeDisp.setText(String.format("%200s",Calendar.getInstance().getTime().toString()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
       });
       timeThread.start();
       application.pack();
       application.setResizable(false);
    }

}
