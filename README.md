# RTCarInspection
Rail Transport Inspections automated via android application for maximum effiecency

Login>>PreInspection>>WWList>>TrainList-->Inspection-->>PostInspection
                                      
Login: With username and password when registered at RT Inspection Center
  >Click button at the bottom of the page after you type in username in the first input area and password in the second input area.

PreInspection: This is the first part of the query for the database.
  >Enter the location, company and train id for the train you are searching for in the designated input areas.
  >You can click the next button to see the Bad Ordered cars that are coming into the station, or Quit to exit back to the Login
   Screen

WWList: This displays the bad ordered cars that are coming into your inspection station
  >You can click next to see the all of the trains in order, with colors green or red designating which cars are currently    
  good(g), or bad(r).
  >You can also click previous to go back to the PreInspection form to reenter a different train-id/location/company.

TrainList: This displays all of the cars that have the train-id that you entered on the PreInspection form. 
  >If the car was bad ordered at the previous stop, it will be red. If the car was not bad ordered at the previous stop it will  
  be green.
  >If you click and hold on a green button it will send you to the Inspection page.
  >If you click on a red button it will ask you to verify that you want to change the status of the car to Green
  >If you click the finish button it will as you if you are done, then send you to the PostInspection page
  
Inspection: Autofills the Reporting Mark and Type of car, then allows you to select if there is a defect, then select which defect specifically there is.
  >If you click on the Defects Switch, it allows you to see what could be wrong with the car.
  >You can then Click on a checkbox for any of the several options for what part of the car could be wrong.
  >Next click on what specifically is wrong with the previous checkbox
  >When you are done recording what is wrong with the car, click next
  >If you click next it will send you back to the TrainList for further Inspection
  
 PostInspection: Displays a summary of the bad ordered cars and what was wrong with them.
  >Click Next Train to get sent back to the details form


