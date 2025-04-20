import SwiftUI
import shared

struct HistoryFullScreen: View {
    
    var body: some View {
        VmView({
            HistoryVm()
        }) { vm, state in
            HistoryFullScreenInner(
                vm: vm,
                state: state
            )
        }
    }
}

///

private struct HistoryFullScreenInner: View {
    
    let vm: HistoryVm
    let state: HistoryVm.State
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        ScrollViewReader { scrollProxy in
            
            ScrollView(.vertical, showsIndicators: false) {
                
                LazyVStack(spacing: 16, pinnedViews: [.sectionHeaders]) {
                    
                    ForEach(state.daysUi, id: \.unixDay) { dayUi in
                        
                        Section(
                            header: Button(
                                action: {
                                    // todo dialog to move to day
                                    withAnimation {
                                        // scrollPosition.scrollTo(id: "day_20185", anchor: .top)
                                    }
                                },
                                label: {
                                    Text(dayUi.dayText)
                                        .padding(.horizontal, 9)
                                        .padding(.vertical, 6)
                                        .background(Capsule().fill(.blue))
                                        .foregroundColor(.white)
                                        .font(.system(size: 14))
                                }
                            )
                            .id("day_\(dayUi.unixDay)")
                        ) {
                            
                            // Warning
                            // VStack to fix https://developer.apple.com/forums/thread/741406
                            
                            VStack(spacing: 10) {
                                
                                ForEach(dayUi.intervalsUi, id: \.intervalDb.id) { intervalUi in
                                    
                                    HStack(alignment: .top, spacing: 10) {
                                        
                                        Button(
                                            action: {
                                                navigation.sheet {
                                                    HistoryFormTimeSheet(
                                                        strategy: HistoryFormTimeStrategy.Update(
                                                            intervalDb: intervalUi.intervalDb
                                                        )
                                                    )
                                                }
                                            },
                                            label: {
                                                Text(intervalUi.timeString)
                                                    .monospaced()
                                                    .fontWeight(.semibold)
                                                    .foregroundColor(!intervalUi.isStartsPrevDay ? .primary : .clear)
                                            }
                                        )
                                        
                                        VStack {
                                            RoundedRectangle(cornerRadius: 20, style: .continuous)
                                                .fill(intervalUi.activityDb.colorRgba.toColor())
                                                .frame(
                                                    width: 10,
                                                    height: Double(10.limitMin(intervalUi.secondsForBar.toInt() / 50))
                                                )
                                                .padding(.leading, 5)
                                                .padding(.trailing, 5)
                                        }
                                        .frame(maxHeight: .infinity)
                                        
                                        HStack(alignment: .top, spacing: 8) {
                                            
                                            if !intervalUi.isStartsPrevDay {
                                                
                                                Button(
                                                    action: {
                                                        navigation.sheet {
                                                            HistoryFormSheet(
                                                                initIntervalDb: intervalUi.intervalDb
                                                            )
                                                        }
                                                    },
                                                    label: {
                                                        Text(intervalUi.text)
                                                            .fontWeight(.medium)
                                                            .textAlign(.leading)
                                                            .foregroundColor(.primary)
                                                    }
                                                )

                                                Spacer()
                                                
                                                Text(intervalUi.periodString)
                                                    .fontWeight(.light)
                                                    .foregroundColor(.secondary)
                                            }
                                        }
                                        .fillMaxWidth()
                                    }
                                    .padding(.leading, 20)
                                    .padding(.trailing, 20)
                                }
                            }
                        }
                    }
                }
            }
            .contentMargins(.bottom, 16)
            .defaultScrollAnchor(.bottom)
        }
        .toolbar {
            ToolbarItemGroup(placement: .bottomBar) {
                BottomBarAddButton(
                    text: "New Entry",
                    action: {
                        navigation.sheet {
                            HistoryFormSheet(
                                initIntervalDb: nil
                            )
                        }
                    }
                )
                Spacer()
                Button("Close") {
                    dismiss()
                }
            }
        }
    }
}
