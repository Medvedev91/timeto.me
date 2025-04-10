import SwiftUI
import shared

private let secondaryFontSize = 15.0

struct WhatsNewScreen: View {
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        VmView({ WhatsNewVm() }) { vm, state in
            
            List {
                
                Section {
                    
                    ForEach(state.historyItemsUi, id: \.self) { historyItemUi in
                        
                        VStack {
                            
                            HStack {
                                
                                Text(historyItemUi.dateText)
                                    .foregroundColor(.secondary)
                                    .font(.system(size: secondaryFontSize))
                                
                                Spacer()
                                
                                Text(historyItemUi.timeAgoText)
                                    .foregroundColor(.secondary)
                                    .font(.system(size: secondaryFontSize))
                            }
                            
                            Text(historyItemUi.title)
                                .foregroundColor(.primary)
                                .font(.system(size: 17, weight: .bold))
                                .padding(.top, 6)
                                .textAlign(.leading)
                            
                            if let text = historyItemUi.text {
                                Text(text)
                                    .foregroundColor(.secondary)
                                    .font(.system(size: secondaryFontSize))
                                    .padding(.top, 6)
                                    .textAlign(.leading)
                            }
                            
                            if let buttonUi = historyItemUi.buttonUi {
                                if buttonUi == .pomodoro {
                                    Button(buttonUi.text) {
                                        navigation.fullScreen {
                                            ReadmeFullScreen(defaultItem: .basics)
                                        }
                                    }
                                    .foregroundColor(.blue)
                                    .padding(.top, 6)
                                    .textAlign(.leading)
                                } else {
                                    fatalError()
                                }
                            }
                        }
                        .padding(.vertical, 2)
                    }
                }
                .listSectionSeparator(.hidden, edges: [.top, .bottom])
                
                Padding(vertical: 20)
                    .customListItem()
            }
            .listStyle(.plain)
            .environment(\.defaultMinListRowHeight, 0)
            .navigationTitle(state.title)
            .toolbarTitleDisplayMode(.inline)
            .contentMarginsTabBar()
        }
    }
}
