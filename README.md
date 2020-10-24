# Active Redundancy

The purpose of this project is to demonstrate the concept of Active Redundancy and has been built atop the foundations laid by the [Heart Beat Tactic](https://github.com/s1mar/Tactic-Heart-Beat-Context-Self-driving-cars) project.

#### Salient Features
- One can have multiple processes of the same type and yet the active redundancy module will only create new instances if every single one of them fails.
- The Environment Perception module is the focus of the demo, each of its instances when fired up create their own cipher to encrypt/decrypt configuration data before sending it to the supervisor. This ensures that the configuration data can't be corrupted by a malevolent attacker.
 ![Image](https://github.com/s1mar/Active-Redundancy/blob/master/screens/1.jpg?raw=true)
    - As you can see in the image, the Supervisor receives the configuration with the heart beat packet in an encrypted form. In case the module dies, the active redundancy feature will try to create it with the last saved configuration.
- This module uses the Builder pattern and grants the user a great deal of flexibility when it comes to customizing the behaviour.
     ![Image](https://github.com/s1mar/Active-Redundancy/blob/master/screens/3.jpg?raw=true)
    - It takes the heartbeat receiver to determine the health of the subject module.
    - **setTimesToRevive** :The Maximum number of times the fallen module will be revived by our AR(Active Redundancy) module. 
    - **setRevivalAction** : What steps to take to revive the fallen module.
    - **setHealthCheckAction** : After reviving the process, monitor when it comes fully online.
    - **setActionPostHealthCheck (Optional)**: Additional steps to take once the module comes online.
    - **reviveWithLastCapturedOperationalData**: Whether to revive with the last successful configuration packet received with the heart beat.
    ![Image](https://github.com/s1mar/Active-Redundancy/blob/master/screens/2.jpg?raw=true)
        In the above image we can see the attached AR module with the Environment perception, trying to revive it.


