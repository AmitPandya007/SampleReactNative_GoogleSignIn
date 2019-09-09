/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */



import React, {Component} from 'react';
import {StyleSheet, Text, View, NativeModules, Button} from 'react-native';

export default class App extends Component{
constructor(props) {
  super(props);
  this.state = { isOn: false };
  this.updateStatus();
}
turnOn = () => {
  NativeModules.NativeModule_GoogleFit.turnOn();
  this.updateStatus()
}
turnOff = () => {
  NativeModules.NativeModule_GoogleFit.turnOff();
  this.updateStatus()
}
updateStatus = () => {
  NativeModules.NativeModule_GoogleFit.getStatus( (error, isOn)=>{
  this.setState({ isOn: isOn});
})
}
render() {
return (
<View style={styles.container}>
{!this.state.isOn ? 
<Button
style={styles.buttonContainer}
onPress={this.turnOn}
title="GoogleFit Sync Data On"
color="#FF6347"
/> :
<Button
onPress={this.turnOff}
title="GoogleFit Sync Data Off"
color="#FF6347"
/> }
</View>
);
}
}
const styles = StyleSheet.create({
container: {
flex: 1,
justifyContent: 'center',
alignItems: 'center',
},
buttonContainer: {
height: 45,
flexDirection: 'row',
justifyContent: 'center',
alignItems: 'center',
marginBottom: 20,
width: 250,
borderRadius: 30,
}
});
