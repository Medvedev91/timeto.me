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
    
    @State private var scrollPosition = ScrollPosition()
    
    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
            LazyVStack(pinnedViews: [.sectionHeaders]) {
                
                ForEach(state.daysUi, id: \.unixDay) { dayUi in
                    
                    Section(
                        header: Button(
                            action: {
                                // todo dialog to move to day
                                withAnimation {
                                    scrollPosition.scrollTo(id: "day_20185", anchor: .top)
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
                        
                        VStack(spacing: 12) {
                            
                            ForEach(dayUi.intervalsDb, id: \.id) { intervalDb in
                                
                                let intervalUi: HistoryVm.IntervalUi = HistoryVm.IntervalUi.companion.build(
                                    interval: intervalDb,
                                    dayUi: dayUi
                                )
                                
                                HStack(alignment: .top, spacing: 10) {
                                    
                                    HStack(alignment: .center, spacing: 0) {
                                        
                                        if !intervalUi.isStartsPrevDay {
                                            
                                            Text(intervalUi.periodString)
                                                .foregroundColor(.primary)
                                                .font(.system(size: 12, weight: .thin))
                                                .frame(alignment: .leading)
                                            
                                            Spacer()
                                            
                                            Text(intervalUi.timeString)
                                                .foregroundColor(.primary)
                                                .font(.system(size: 15, weight: .medium, design: .monospaced))
                                                .frame(alignment: .trailing)
                                        }
                                    }
                                    .frame(width: 100, alignment: .leading)
                                    .padding(.top, 3)
                                    
                                    VStack {
                                        
                                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                                            .fill(intervalDb.selectActivityDbCached().colorRgba.toColor())
                                            .frame(
                                                width: 10,
                                                height: Double(10.limitMin(intervalUi.secondsForBar.toInt() / 50))
                                            )
                                            .padding(.leading, 5)
                                            .padding(.trailing, 5)
                                    }
                                    .frame(maxHeight: .infinity)
                                    
                                    VStack(spacing: 4) {
                                        
                                        if !intervalUi.isStartsPrevDay {
                                            
                                            Text(intervalUi.text)
                                                .foregroundColor(.primary)
                                                .font(.system(size: 16, weight: .medium))
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.top, 3)
                                }
                                .padding(.leading, 20)
                                .padding(.trailing, 20)
                            }
                        }
                    }
                }
            }
        }
        .contentMargins(.bottom, 8)
        .defaultScrollAnchor(.bottom)
        .scrollPosition($scrollPosition, anchor: .top)
        .toolbar {
            ToolbarItemGroup(placement: .bottomBar) {
                BottomBarAddButton(
                    text: "New Entry",
                    action: {
                        // todo
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
