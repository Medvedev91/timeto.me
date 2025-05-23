import SwiftUI
import shared

//
// Separate class to call init() only if necessary. todo Is it actual?
 
struct WatchTickerDialog: View {
    
    var activity: ActivityDb
    let task: TaskDb?
    private let preAdd: () -> Void
    
    // Int32 для соответствия типа с TimerPickerItem.seconds
    @State private var formSeconds: Int32
    private let timeItems: [TimerPickerItem]
    
    init(
        activity: ActivityDb,
        task: TaskDb?,
        preAdd: @escaping () -> Void
    ) {
        self.activity = activity
        
        let defSeconds = TimerPickerItem.companion.calcDefSeconds(activity: activity, note: task?.text)
        _formSeconds = State(initialValue: defSeconds)
        timeItems = TimerPickerItem.companion.buildList(defSeconds: defSeconds)
        
        self.preAdd = preAdd
        self.task = task
    }
    
    var body: some View {
        
        VStack {
            
            Picker("Timer", selection: self.$formSeconds) {
                ForEach(timeItems, id: \.seconds) { item in
                    Text(item.title)
                }
            }
            .padding(.vertical, 10)
            .labelsHidden()
            
            Button(
                action: {
                    if let task = task {
                        WatchToIosSync.shared.startTaskWithLocal(
                            activity: activity,
                            timer: formSeconds,
                            task: task
                        )
                    } else {
                        WatchToIosSync.shared.startIntervalWithLocal(
                            activity: activity,
                            seconds: formSeconds
                        )
                    }
                    preAdd()
                },
                label: {
                    Text("Start")
                        .foregroundColor(.blue)
                }
            )
            .buttonStyle(.plain)
        }
    }
}
