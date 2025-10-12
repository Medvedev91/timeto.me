import SwiftUI
import shared

struct WatchTabTimerView: View {
    
    var body: some View {
        
        VmView({
            WatchTabTimerVm()
        }) { vm, state in
            
            ScrollViewReader { scrollProxy in
                
                VStack {
                    
                    // Tried by List, but listRowBackground() does not work on screen transition.
                    ScrollView(.vertical, showsIndicators: false) {
                        
                        VStack(spacing: 5) {
                            
                            TimerView()
                                .id("timer_view")
                                .padding(.bottom, 16)
                            
                            ForEach(state.activitiesUI, id: \.goalDb.id) { activityUI in
                                ActivityView(activityUI: activityUI)
                                    .id("aid__\(activityUI.goalDb.id)")
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
        
        var activityUI: WatchTabTimerVm.ActivityUI
        
        let defBgColor = Color(r: 34, g: 34, b: 35, a: 255)
        
        var body: some View {
            
            Button(
                action: {
                    activityUI.startDefaultTimer()
                },
                label: {
                    
                    VStack {
                        
                        HStack(alignment: .center) {
                            
                            Text(activityUI.text)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .lineLimit(1)
                                .truncationMode(.middle)
                        }
                        
                        if !activityUI.timerHintsUi.isEmpty {
                            HStack(spacing: 6) {
                                ForEach(activityUI.timerHintsUi, id: \.timer) { hintUi in
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
