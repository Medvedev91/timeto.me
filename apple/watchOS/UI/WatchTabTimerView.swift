import SwiftUI
import shared

struct WatchTabTimerView: View {
    
    var body: some View {
        
        VmView({
            WatchTabTimerVm()
        }) { vm, state in
            let state = vm.state.value as! WatchTabTimerVm.State
            
            ScrollViewReader { scrollProxy in
                
                VStack {
                    
                    // Tried by List, but listRowBackground() does not work on screen transition.
                    ScrollView(.vertical, showsIndicators: false) {
                        
                        VStack(spacing: 5) {
                            
                            TimerView()
                                .id("timer_view")
                                .padding(.bottom, 16)
                            
                            ForEach(state.activitiesUi, id: \.activityDb.id) { activityUi in
                                ActivityView(activityUi: activityUi)
                                    .id("aid__\(activityUi.activityDb.id)")
                            }
                        }
                    }
                }
                .onChange(of: state.lastInterval) { _, newLastInterval in
                    scrollProxy.scrollTo("timer_view", anchor: .top)
                }
            }
        }
    }
    
    struct TimerView: View {
        
        var body: some View {
            
            VmView({
                WatchTimerVm()
            }) { vm, state in
                let state = vm.state.value as! WatchTimerVm.State
                VStack {
                    
                    Text(state.timerData.note)
                        .foregroundColor(state.timerData.noteColor.toColor())
                        .font(.system(size: 15))
                    
                    Text(state.timerData.timerText)
                        .font(.system(size: 35, weight: .bold, design: .monospaced))
                        .fontWeight(.medium)
                        .foregroundColor(state.timerData.timerColor.toColor())
                }
                .onTapGesture {
                    vm.togglePomodoro()
                }
            }
        }
    }
    
    struct ActivityView: View {
        
        var activityUi: WatchTabTimerVm.ActivityUi
        
        let defBgColor = Color(r: 34, g: 34, b: 35, a: 255)
        
        var body: some View {
            
            Button(
                action: {
                    activityUi.startDefaultTimer()
                },
                label: {
                    
                    VStack {
                        
                        HStack(alignment: .center) {
                            
                            Text(activityUi.text)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .lineLimit(1)
                                .truncationMode(.middle)
                        }
                        
                        if !activityUi.timerHintsUi.isEmpty {
                            HStack(spacing: 6) {
                                ForEach(activityUi.timerHintsUi, id: \.timer) { hintUi in
                                    Button(
                                        action: {
                                            hintUi.startInterval()
                                        },
                                        label: {
                                            Text(hintUi.text)
                                                .font(.system(size: 13, weight: .medium))
                                                .foregroundColor(.white)
                                        }
                                    )
                                    .buttonStyle(.borderless)
                                }
                                Spacer()
                            }
                        }
                    }
                    .background(defBgColor.opacity(0.001))
                }
            )
            .frame(minHeight: 36) // Approximately
            .padding([.horizontal], 8)
            .padding([.vertical], 4)
            .buttonStyle(.plain)
            .background(squircleShape.fill(defBgColor))
        }
    }
}
